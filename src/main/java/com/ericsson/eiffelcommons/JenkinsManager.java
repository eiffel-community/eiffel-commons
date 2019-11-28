/*
   Copyright 2019 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.eiffelcommons;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ericsson.eiffelcommons.helpers.MediaType;
import com.ericsson.eiffelcommons.http.HttpRequest;
import com.ericsson.eiffelcommons.http.ResponseEntity;
import com.ericsson.eiffelcommons.http.HttpRequest.HttpMethod;

public class JenkinsManager {

    private String jenkinsBaseUrl;
    private String encoding;
    private String crumb;

    /**
     * Constructor, takes jenkins baseUrl, username and password.
     *
     * @param jenkinsBaseUrl
     *            :: Jenkins base url http://somehost:123
     * @param username
     *            :: Jenkins username as String
     * @param password
     *            :: Jenkins password or API-token
     * @throws URISyntaxException
     * @throws JSONException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public JenkinsManager(String jenkinsBaseUrl, String username, String password)
            throws URISyntaxException, JSONException, ClientProtocolException, IOException {
        this.jenkinsBaseUrl = jenkinsBaseUrl;
        this.encoding = createEncodingFromUsernameAndPassword(username, password);
        this.crumb = getCrumb();
    }

    /**
     * Constructor, takes jenkins protocol, host, port, username and password.
     *
     * @param protocol
     *            :: http/https protocol to jenkins as String
     * @param host
     *            :: Host name to jenkins as String
     * @param port
     *            :: Port number to jenkins as int
     * @param username
     *            :: Jenkins username as String
     * @param password
     *            :: Jenkins password or API-token
     * @throws URISyntaxException
     * @throws JSONException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public JenkinsManager(String protocol, String host, int port, String username, String password)
            throws URISyntaxException, JSONException, ClientProtocolException, IOException {
        this.jenkinsBaseUrl = String.format("%s://%s:%d", protocol, host, port);
        this.encoding = createEncodingFromUsernameAndPassword(username, password);
        this.crumb = getCrumb();
    }

    /**
     * Creates a jenkins job with a given name using the XML data as input for job configuration
     *
     * @param jobName
     *            :: Name of job as String
     * @param jobXmlData
     *            :: XML data as String
     * @return
     * @throws Exception
     */
    public boolean createJob(String jobName, String jobXmlData) throws Exception {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST);
        boolean success = false;

        if (jobName == null || jobName.isEmpty()) {
            throw new Exception("A job is no one! Jenkins do not like no one!");
        }

        httpRequest.setBaseUrl(jenkinsBaseUrl)
                   .addHeader("Authorization", "Basic " + encoding)
                   .addHeader("Content-type", MediaType.APPLICATION_XML)
                   .addHeader("Jenkins-Crumb", crumb)
                   .addParameter("name", jobName)
                   .setBody(jobXmlData)
                   .setEndpoint("/createItem");

        ResponseEntity response = httpRequest.performRequest();
        success = response.getStatusCode() == HttpStatus.SC_OK;

        if (!success) {
            String message = "Failed to create a jenkins job with name " + jobName + " using jenkins crumb " + crumb
                    + ".\nAnd job data:\n" + jobXmlData + "\nStatus code was: " + response.getStatusCodeValue() + ".";
            throw new Exception(message);
        }

        return success;
    }

    /**
     * Creates a jenkins job with a given name using the XML data as input for job configuration.
     *
     * Warning: Deletes old job if exist.
     *
     * @param jobName
     *            :: Name of job as String
     * @param jobXmlData
     *            :: XML data as String
     * @return
     * @throws Exception
     */
    public boolean forceCreateJob(String jobName, String jobXmlData) throws Exception {
        try {
            deleteJob(jobName);
        } catch(Exception e) {}

        return createJob(jobName, jobXmlData);
    }

    /**
     * This function that trigger a build on a jenkins job
     *
     * @param jobName
     *            :: Name of job as String
     * @param jobToken
     *            :: Token used to trigger the job as String
     * @return
     * @throws Exception
     */
    public boolean buildJob(String jobName, String jobToken) throws Exception {
        String buildType = "build";
        boolean success = executeJobTriggering(jobName, jobToken, buildType, MediaType.APPLICATION_FORM_URLENCODED,
                null, null);

        return success;
    }

    /**
     * This function that trigger a build on a jenkins job with given body
     *
     * @param jobName
     *            :: Name of job as String
     * @param jobToken
     *            :: Token used to trigger the job as String
     * @param body
     *            :: Body as string "json={param:"param"}"
     * @return
     * @throws Exception
     */
    public boolean buildJobWithFormPostParams(String jobName, String jobToken, String body) throws Exception {
        String buildType = "build";
        boolean success = executeJobTriggering(jobName, jobToken, buildType, MediaType.APPLICATION_FORM_URLENCODED,
                null, body);

        return success;
    }

    /**
     * This function that trigger a build on a jenkins job with given parameters
     *
     * @param jobName
     *            :: Name of job as String
     * @param jobToken
     *            :: Token used to trigger the job as String
     * @param parameters
     *            :: Parameters as Map"
     * @return
     * @throws Exception
     */
    public boolean buildJobWithParameters(String jobName, String jobToken, Map<String, String> parameters)
            throws Exception {
        String buildType = "buildWithParameters";
        boolean success = executeJobTriggering(jobName, jobToken, buildType, MediaType.APPLICATION_JSON, parameters,
                null);

        return success;
    }

    /**
     * This function recieves a jenkins job name and and a build number, then returns the build status as JSONObject.
     *
     * @param jobName
     *            :: Name of job as String
     * @param buildNumber
     *            :: build number as Integer, defaults to lastSuccessfulBuild
     * @return JSONObject
     * @throws Exception
     */
    public JSONObject getJenkinsBuildStatusData(String jobName, Integer buildNumber) throws Exception {
        boolean dataRecieved = false;
        String buildNumberString = "lastBuild";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);

        if (jobName == null || jobName.isEmpty()) {
            throw new Exception("Cannot get job data without a job name.");
        }
        if (buildNumber != null) {
            buildNumberString = buildNumber.toString();
        }
        String endpoint = "/job/" + jobName + "/" + buildNumberString + "/api/json";

        httpRequest.setBaseUrl(jenkinsBaseUrl)
                   .addHeader("Authorization", "Basic " + encoding)
                   .addHeader("Content-type", MediaType.APPLICATION_JSON)
                   .setEndpoint(endpoint);

        ResponseEntity response = httpRequest.performRequest();
        dataRecieved = response.getStatusCode() == HttpStatus.SC_OK;

        if (!dataRecieved) {
            String message = "Failed to job data from job " + jobName + " and build " + buildNumberString
                    + ". Status code: " + response.getStatusCodeValue() + ". Possible not built yet.";
            throw new Exception(message);
        }

        JSONObject jsonObj = new JSONObject(response.getBody());
        return jsonObj;
    }

    /**
     * This function recieves a jenkins job name and returns the build status as JSONObject.
     *
     * @param jobName
     *            :: Name of job as String
     * @return JSONObject
     * @throws Exception
     */
    public JSONObject getJenkinsBuildStatusData(String jobName) throws Exception {
        return getJenkinsBuildStatusData(jobName, null);
    }

    /**
     *
     * This function recieves a jenkins job name and deletes that job from the jenkins system
     *
     * @param jobName
     *            :: Name of job as String
     * @return JSONObject
     * @throws Exception
     */
    public boolean deleteJob(String jobName) throws Exception {
        boolean isDeleted = false;
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST);
        if (jobName == null || jobName.isEmpty()) {
            throw new Exception("'No one' cannot be deleted from jenkins.");
        }

        String endpoint = "/job/" + jobName + "/doDelete";

        httpRequest.setBaseUrl(jenkinsBaseUrl)
                   .addHeader("Authorization", "Basic " + encoding)
                   .addHeader("Content-type", MediaType.APPLICATION_JSON)
                   .addHeader("Jenkins-Crumb", crumb)
                   .setEndpoint(endpoint);

        ResponseEntity response = httpRequest.performRequest();
        isDeleted = response.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY;

        if (!isDeleted) {
            String message = "Failed to delete jenkins job " + jobName + ". Status code: "
                    + response.getStatusCodeValue() + ".";
            throw new Exception(message);
        }

        return isDeleted;
    }

    /**
     * Detect if jenkins has a given plugin.
     *
     * @param plugin
     *            :: Plugin name
     * @return
     * @throws Exception
     */
    public boolean pluginExists(String plugin) throws Exception {
        boolean pluginExists = false;

        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);
        httpRequest.setBaseUrl(jenkinsBaseUrl)
                   .addHeader("Authorization", "Basic " + encoding)
                   .addParameter("depth", "1")
                   .addParameter("wrapper", "plugins")
                   .setEndpoint("/pluginManager/api/json");

        ResponseEntity response = httpRequest.performRequest();
        boolean success = response.getStatusCode() == HttpStatus.SC_OK;

        if (!success) {
            String message = "Failed to fetch list of plugins from jenkins. Response code: "
                    + response.getStatusCodeValue();
            throw new Exception(message);
        }

        JSONObject responseData = new JSONObject(response.getBody());
        JSONArray pluginList = responseData.getJSONArray("plugins");

        for (int i = 0; i < pluginList.length(); i++) {
            String foundPlugin = pluginList.getJSONObject(i)
                                           .getString("shortName");
            if (plugin.equalsIgnoreCase(foundPlugin)) {
                pluginExists = true;
                break;
            }
        }

        return pluginExists;
    }

    /**
     * Install a given plugin into jenkins.
     *
     * @param plugin
     *            :: Plugin name
     * @param version
     *            :: Plugin version
     * @return
     * @throws Exception
     */
    public boolean installPlugin(String plugin, String version) throws Exception {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST);
        boolean success = false;

        if (plugin == null || plugin.isEmpty()) {
            throw new Exception("'No plugin' cannot be added to jenkins.");
        }
        if (version == null || version.isEmpty()) {
            throw new Exception("A version must be speciified for the Jenkins Plugin.");
        }

        String scriptData = "<jenkins><install plugin='" + plugin + "@" + version + "' /></jenkins>";

        httpRequest.setBaseUrl(jenkinsBaseUrl)
                   .addHeader("Authorization", "Basic " + encoding)
                   .addHeader("Content-type", MediaType.TEXT_XML)
                   .addHeader("Jenkins-Crumb", crumb)
                   .setBody(scriptData)
                   .setEndpoint("/pluginManager/installNecessaryPlugins");

        ResponseEntity response = httpRequest.performRequest();
        success = response.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY;

        if (!success) {
            String message = "Failed to add a plugin with name " + plugin + " and version " + version
                    + " to Jenkins. Response code: " + response.getStatusCodeValue() + " and body: "
                    + response.getBody();
            throw new Exception(message);
        }

        return success;
    }

    /**
     * Trigger a restart of jenkins, returns true if restart was successfull.
     *
     * @return
     * @throws Exception
     */
    public boolean restartJenkins() throws Exception {
        boolean success = false;
        boolean restartVerified = false;

        ResponseEntity response = null;
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST);
        httpRequest.setBaseUrl(jenkinsBaseUrl)
                   .addHeader("Authorization", "Basic " + encoding)
                   .addHeader("Content-type", MediaType.APPLICATION_JSON)
                   .addHeader("Jenkins-Crumb", crumb)
                   .setEndpoint("/safeRestart");

        response = httpRequest.performRequest();
        success = response.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY;

        if (!success) {
            String message = "Failed to restart Jenkins. Response code: " + response.getStatusCodeValue()
                    + " and body: " + response.getBody();
            throw new Exception(message);
        }

        restartVerified = verifyJenkinsRestart();

        return success && restartVerified;
    }

    /**
     * Executes job triggering with given parameters or body if any.
     *
     * @param jobName
     * @param jobToken
     * @param buildType
     * @param mediatype
     * @param parameters
     * @param body
     * @return
     * @throws Exception
     */
    private boolean executeJobTriggering(String jobName, String jobToken, String buildType, String mediatype,
                                         Map<String, String> parameters, String body)
            throws Exception {
        jobNameTokenValidation(jobName, jobToken);
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);
        String endpoint = "/job/" + jobName + "/" + buildType;

        if (parameters != null) {
            httpRequest.addParameters(parameters);
        }
        if (body != null) {
            httpRequest.setBody(body);
        }

        httpRequest.setBaseUrl(jenkinsBaseUrl)
                   .addHeader("Authorization", "Basic " + encoding)
                   .addHeader("Content-type", mediatype)
                   .addParameter("token", jobToken)
                   .setEndpoint(endpoint);

        ResponseEntity response = httpRequest.performRequest();
        Boolean success = response.getStatusCode() == HttpStatus.SC_CREATED;

        if (!success) {
            String message = "Failed to trigger a jenkins job " + jobName + " using token " + jobToken
                    + " Status code: " + response.getStatusCodeValue() + ".";
            throw new Exception(message);
        }
        return success;
    }

    /**
     * Verifies that name and token is not null or empty.
     *
     * @param jobName
     * @param jobToken
     * @throws Exception
     */
    private void jobNameTokenValidation(String jobName, String jobToken) throws Exception {
        if (jobName == null || jobName.isEmpty()) {
            throw new Exception("Cannot trigger a jenkins job without a job name.");
        }
        if (jobToken == null) {
            throw new Exception("A job token is required to trigger a jenkins job.");
        }
    }

    /**
     * Function that makes a get request towards jenkins for 60 seconds or untill jenkins is responding. Returns true if
     * jenkins was down when it started and became available. Returns false if jenkins never responded or never went
     * down.
     *
     * @return
     * @throws Exception
     */
    private boolean verifyJenkinsRestart() throws Exception {
        boolean success = false;
        boolean serverDownRecieved = false;
        ResponseEntity response = null;

        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);
        httpRequest.setBaseUrl(jenkinsBaseUrl)
                   .addHeader("Authorization", "Basic " + encoding)
                   .setEndpoint("/api/json");

        long stopTime = System.currentTimeMillis() + 60000;
        do {
            Thread.sleep(3000);
            response = httpRequest.performRequest();
            serverDownRecieved = response.getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE || serverDownRecieved;
            success = response.getStatusCode() == HttpStatus.SC_OK || success;
        } while (!success && stopTime > System.currentTimeMillis());

        if (!success) {
            String message = "Could not verify that Jenkins started up correctly. Response code: "
                    + response.getStatusCodeValue() + " and body: " + response.getBody();
            throw new Exception(message);
        }

        return success && serverDownRecieved;
    }

    /**
     * This function creates an encoded parameter as a String.
     *
     * @param username
     * @param password
     * @return
     * @throws UnsupportedEncodingException
     */
    private String createEncodingFromUsernameAndPassword(String username, String password)
            throws UnsupportedEncodingException {
        String authString = String.join(":", username, password);
        String encoding = DatatypeConverter.printBase64Binary(authString.getBytes("utf-8"));
        return encoding;
    }

    /**
     * This function fetches a crumb from Jenkins.
     *
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws ClientProtocolException
     * @throws Exception
     */
    private String getCrumb() throws ClientProtocolException, URISyntaxException, IOException {
        String crumb = "";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);

        httpRequest.setBaseUrl(jenkinsBaseUrl)
                   .addHeader("Authorization", "Basic " + encoding)
                   .addHeader("Content-type", MediaType.APPLICATION_JSON)
                   .setEndpoint("/crumbIssuer/api/json");

        ResponseEntity response = httpRequest.performRequest();
        boolean success = response.getStatusCode() == HttpStatus.SC_OK;

        if (success) {
            JSONObject jsonObj = new JSONObject(response.getBody());
            crumb = jsonObj.getString("crumb");
        }

        return crumb;
    }
}
