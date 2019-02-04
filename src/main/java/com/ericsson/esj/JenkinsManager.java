package com.ericsson.esj;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.esj.helpers.MediaType;

import utils.HttpRequest;
import utils.ResponseEntity;
import utils.HttpRequest.HttpMethod;


public class JenkinsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(JenkinsManager.class);

    private static final String JENKINS_JOB_TEMPLATE_FILE_PATH = String.join(File.separator, "src", "main",
            "resources", "jenkinsJobTemplate.xml");
    private static final String DEFAULT_JOB_TOKEN = "test";
    private static final String DEFAULT_SCRIPT = "echo &quot;Test&quot;";
    private static final String DEFAULT_JOB_NAME = "test_job";

    private String jenkinsBaseUrl;
    private String encoding;
    private String crumb;

    /**
     * Constructor, takes jenkins baseUrl, username and password.
     * @param jenkinsBaseUrl
     * @param username
     * @param password   **  Jenkins password or API-token
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     * @throws JSONException
     */
    public JenkinsManager(String jenkinsBaseUrl, String username, String password)
            throws UnsupportedEncodingException, URISyntaxException, JSONException {
        String authString = String.join(":", username, password);

        this.jenkinsBaseUrl = jenkinsBaseUrl;
        this.encoding = DatatypeConverter.printBase64Binary(authString.getBytes("utf-8"));
        this.crumb = getCrumb();
    }

    /**
     * Constructor, takes jenkins protocol, host, port, username and password.
     * @param protocol
     * @param host
     * @param port
     * @param username
     * @param password   **  Jenkins password or API-token
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     * @throws JSONException
     */
    public JenkinsManager(String protocol, String host, int port, String username, String password)
            throws UnsupportedEncodingException, URISyntaxException, JSONException {
        String authString = String.join(":", username, password);
        this.jenkinsBaseUrl = String.format("%s://%s:%d",protocol, host, port);
        this.encoding = DatatypeConverter.printBase64Binary(authString.getBytes("utf-8"));
        this.crumb = getCrumb();
    }

    /**
     * Takes a token and script and returns XML data for jenkins with job_token and
     * script added.
     *
     * @param job_token
     * @param script
     * @return
     * @throws IOException
     */
    public String getXmlJobData(String job_token, String script) throws IOException {
        String jobData = getStringFromFile(JENKINS_JOB_TEMPLATE_FILE_PATH);

        if (job_token == null) {
            job_token = DEFAULT_JOB_TOKEN;
        }
        if (script == null) {
            script = DEFAULT_SCRIPT;
        }

        jobData = injectScriptAndTokenIntoXmlString(jobData, job_token, script);
        return jobData;
    }

    /**
     * Creates a jenkins job with a given name using the XML data as input for
     * job configuration
     *
     * @param jobName
     * @param jobXmlData
     * @return
     * @throws URISyntaxException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public boolean createJob(String jobName, String jobXmlData) throws URISyntaxException {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST);
        boolean success = false;

        if (jobName == null) {
            jobName = DEFAULT_JOB_NAME;
        }

        httpRequest.setJenkinsBaseUrl(jenkinsBaseUrl).addHeader("Authorization", "Basic " + encoding)
                .addHeader("Content-type", MediaType.APPLICATION_XML).addHeader("Jenkins-Crumb", crumb)
                .addParam("name", jobName).setBody(jobXmlData).setEndpoint("/createItem");

        ResponseEntity response = httpRequest.performRequest();
        success = response.getStatusCode() == HttpStatus.SC_OK;

        if(!success) {
            LOGGER.error("Failed to create a jenkins job. Status code: " + response.getStatusCodeValue() + ". Reason: " + response.getBody());
        }

        return success;
    }

    /**
     * This function recieves a jenkins job name and are used to trigger the job
     *
     * @throws URISyntaxException
     */
    public boolean triggerJob(String jobName, String job_token) throws URISyntaxException {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);
        boolean success = false;

        if (jobName == null) {
            jobName = DEFAULT_JOB_NAME;
        }
        if (job_token == null) {
            job_token = DEFAULT_JOB_TOKEN;
        }

        String endpoint = "/job/" + jobName + "/build";

        httpRequest.setJenkinsBaseUrl(jenkinsBaseUrl).addHeader("Authorization", "Basic " + encoding)
                .addHeader("Content-type", MediaType.APPLICATION_JSON).addParam("token", job_token)
                .setEndpoint(endpoint);

        ResponseEntity response = httpRequest.performRequest();
        success = response.getStatusCode() == HttpStatus.SC_CREATED;

        if(!success) {
            LOGGER.error("Failed to trigger a jenkins job. Status code: " + response.getStatusCodeValue() + ". Reason: " + response.getBody());
        }

        return success;
    }

    /**
     * This function recieves a jenkins job name and returns whether the job has
     * been triggered at least once
     *
     * @param jobName
     * @return
     * @throws URISyntaxException
     */
    public boolean jobHasBeenTriggered(String jobName) throws URISyntaxException {
        boolean isTriggered = false;
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);
        if (jobName == null) {
            jobName = DEFAULT_JOB_NAME;
        }

        String endpoint = "/job/" + jobName + "/1/api/json";

        httpRequest.setJenkinsBaseUrl(jenkinsBaseUrl).addHeader("Authorization", "Basic " + encoding)
                .addHeader("Content-type", MediaType.APPLICATION_JSON)
                .setEndpoint(endpoint);

        ResponseEntity response = httpRequest.performRequest();
        isTriggered = response.getStatusCode() == HttpStatus.SC_OK;

        if(!isTriggered) {
            LOGGER.error("Failed to check if job was triggered. Status code: " + response.getStatusCodeValue() + ". Reason: " + response.getBody());
        }

        return isTriggered;
    }

    /**
     *
     * This function recieves a jenkins job name and deletes that job from the
     * jenkins system
     *
     * @param jobName
     * @return
     * @throws URISyntaxException
     */
    public boolean deleteJob(String jobName) throws URISyntaxException {
        boolean isDeleted = false;
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST);
        if (jobName == null) {
            jobName = DEFAULT_JOB_NAME;
        }

        String endpoint = "/job/" + jobName + "/doDelete";

        httpRequest.setJenkinsBaseUrl(jenkinsBaseUrl).addHeader("Authorization", "Basic " + encoding)
                .addHeader("Content-type", MediaType.APPLICATION_JSON).addHeader("Jenkins-Crumb", crumb)
                .setEndpoint(endpoint);

        ResponseEntity response = httpRequest.performRequest();
        isDeleted = response.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY;

        if(!isDeleted) {
            LOGGER.error("Failed to delete jenkins job. Status code: " + response.getStatusCodeValue() + ". Reason: " + response.getBody());
        }

        return isDeleted;
    }

    private String getCrumb() throws URISyntaxException, JSONException {
        String crumb = "";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);

        httpRequest.setJenkinsBaseUrl(jenkinsBaseUrl).addHeader("Authorization", "Basic " + encoding)
                .addHeader("Content-type", MediaType.APPLICATION_JSON).setEndpoint("/crumbIssuer/api/json");

        ResponseEntity response = httpRequest.performRequest();
        System.out.println("HOW == " + response);
        boolean success = response.getStatusCode() == HttpStatus.SC_OK;

        if (success) {
            JSONObject jsonObj = new JSONObject(response.getBody());
            crumb = jsonObj.getString("crumb");
        } else {
            LOGGER.error("Failed to get jenkins crumb. Status code: " + response.getStatusCodeValue() + ". Reason: " + response.getBody());
        }

        return crumb;
    }

    private static String getStringFromFile(String filepath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filepath)), StandardCharsets.UTF_8);
    }

    private String injectScriptAndTokenIntoXmlString(String xmlString, String token, String script) {
        xmlString = xmlString.replaceAll("\\$\\{shell\\.script\\}", script);
        xmlString = xmlString.replaceAll("\\$\\{auth\\.token\\}", token);
        return xmlString;
    }

}
