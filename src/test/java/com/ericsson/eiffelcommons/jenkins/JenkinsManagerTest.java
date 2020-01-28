package com.ericsson.eiffelcommons.jenkins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Parameter;

import com.ericsson.eiffelcommons.exceptions.JenkinsManagerException;

public class JenkinsManagerTest {

    private static final String UNKNOWN_HOST = "!¤€31";
    private static final int PORT_OUT_OF_RANGE = 1000000;
    private static final String INVALID_PROTOCOL = "httg";
    private static final String URL = "http://localhost";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String PROTOCOL = "http";
    private static final String HOST = "localhost";
    private static final String ENDPOINT_CRUMB = "/crumbIssuer/api/json";
    private static final String RESPONSE_CRUMB = "{\"crumb\":\"fb171d526b9cc9e25afe80b356e12cb7\",\"crumbRequestField\":\".crumb\"}";
    private static final String JOB_NAME = "JenkinsJob";
    private static final String XML = "<?xml version=\"1.0\"?><test>Test</test>";
    private static final String ENDPOINT_CREATE = "/createItem";
    private static final String HEADER_AUTH = "Authorization";
    private static final String ENDPOINT_DELETE = String.format("/job/%s/doDelete", JOB_NAME);
    private static final String TOKEN_KEY = "token";
    private static final String TOKEN_VALUE = "myToken";
    private static final String ENDPOINT_BUILD = String.format("/job/%s/build", JOB_NAME);
    private static final String BODY = "{}";
    private static final String BODY_JSON = "json={}";
    private static final String PARAMETER_KEY = "key";
    private static final String PARAMETER_VALUE = "value";
    private static final String ENDPOINT_BUILD_PARAMETERS = String.format(
            "/job/%s/buildWithParameters", JOB_NAME);
    private static final int BUILD_NUMBER = 1;
    private static final String ENDPOINT_STATUS = String.format("/job/%s/%s/api/json", JOB_NAME,
            BUILD_NUMBER);
    private static final String ENDPOINT_STATUS_FALLBACK = String.format(
            "/job/%s/lastBuild/api/json", JOB_NAME);
    private static final String ENDPOINT_PLUGIN = "/pluginManager/api/json";
    private static final String PLUGIN_NAME = "myPlugin";
    private static final String BODY_PLUGIN = String.format(
            "{\"plugins\":[{\"shortName\":\"%s\"}]}", PLUGIN_NAME);
    private static final String PLUGIN_NOT_FOUND = "dummy";
    private static final String ENDPOINT_PLUGIN_INSTALL = "/pluginManager/installNecessaryPlugins";
    private static final String PLUGIN_VERSION = "1.0.0";
    private static final String BODY_PLUGIN_INSTALL = String.format(
            "<jenkins><install plugin='%s@%s'/></jenkins>", PLUGIN_NAME, PLUGIN_VERSION);
    private static final String ENDPOINT_RESTART = "/safeRestart";
    private static final String ENDPOINT_JENKINS = "/api/json";

    private static HashMap<String, String> parametersMap = new HashMap<String, String>();

    private static ClientAndServer mockServer;
    private static int port;

    @BeforeClass
    public static void beforeTest() throws IOException {
        port = findOpenPort();
        mockServer = startClientAndServer(port);
        parametersMap.put("key", "value");
    }

    @Test
    public void jenkinsManagerConstructorURL()
            throws JSONException, ClientProtocolException, URISyntaxException, IOException {
        setUpCrumbEndpoint();
        JenkinsManager jenkins = new JenkinsManager(URL + ":" + port, USERNAME, PASSWORD);
        String actualCrumb = jenkins.getCrumb();
        String expectedCrumb = new JSONObject(RESPONSE_CRUMB).getString("crumb");
        assertEquals(expectedCrumb, actualCrumb);
        String actualUrl = jenkins.getJenkinsBaseUrl();
        String expectedUrl = URL + ":" + port;
        assertEquals(expectedUrl, actualUrl);
        String actualEncoding = jenkins.getEncoding();
        String expectedEncoding = createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        assertEquals(expectedEncoding, actualEncoding);
    }

    @Test
    public void jenkinsManagerConstructorURLBuild()
            throws JSONException, ClientProtocolException, URISyntaxException, IOException {
        setUpCrumbEndpoint();
        JenkinsManager jenkins = new JenkinsManager(PROTOCOL, HOST, port, USERNAME, PASSWORD);
        String actualCrumb = jenkins.getCrumb();
        String expectedCrumb = new JSONObject(RESPONSE_CRUMB).getString("crumb");
        assertTrue(actualCrumb.equals(expectedCrumb));
    }

    @Test
    public void jenkinsManagerConstructorFailCrumb()
            throws JSONException, ClientProtocolException, URISyntaxException, IOException {
        setUpFailCrumbEndpoint();
        JenkinsManager jenkins = new JenkinsManager(URL + ":" + port, USERNAME, PASSWORD);
        String actualCrumb = jenkins.getCrumb();
        assertEquals("", actualCrumb);
    }

    @Test(expected = MalformedURLException.class)
    public void jenkinsManagerConstructorInvalidProtocol()
            throws ClientProtocolException, URISyntaxException, IOException {
        new JenkinsManager(INVALID_PROTOCOL, HOST, port, USERNAME, PASSWORD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void jenkinsManagerConstructorPortOutOfRange()
            throws ClientProtocolException, URISyntaxException, IOException {
        new JenkinsManager(PROTOCOL, HOST, PORT_OUT_OF_RANGE, USERNAME, PASSWORD);
    }

    @Test(expected = UnknownHostException.class)
    public void jenkinsManagerConstructorUnknownHost()
            throws ClientProtocolException, URISyntaxException, IOException {
        new JenkinsManager(PROTOCOL, UNKNOWN_HOST, port, USERNAME, PASSWORD);
    }

    @Test
    public void createJob() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpCreateEndpoint();
        boolean success = jenkins.createJob(JOB_NAME, XML);
        assertTrue(success);
    }

    @Test(expected = JenkinsManagerException.class)
    public void createJobNameNull() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        jenkins.createJob(null, XML);
    }

    @Test(expected = JenkinsManagerException.class)
    public void createJobNameEmpty() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        jenkins.createJob("", XML);
    }

    @Test(expected = JenkinsManagerException.class)
    public void createJobFailRequest() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpCreateEndpointFail();
        jenkins.createJob(JOB_NAME, XML);
    }

    @Test
    public void deleteJob() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpDeleteEndpoint();
        boolean success = jenkins.deleteJob(JOB_NAME);
        assertTrue(success);
    }

    @Test(expected = JenkinsManagerException.class)
    public void deleteJobFailRequest() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpDeleteEndpointFail();
        jenkins.deleteJob(JOB_NAME);
    }

    @Test(expected = JenkinsManagerException.class)
    public void deleteJobNameNull() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        jenkins.deleteJob(null);
    }

    @Test(expected = JenkinsManagerException.class)
    public void deleteJobNameEmpty() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        jenkins.deleteJob("");
    }

    @Test
    public void forceCreateJob() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpDeleteAndCreateEndpoint();
        boolean success = jenkins.forceCreateJob(JOB_NAME, XML);
        assertTrue(success);
    }

    @Test
    public void buildJob() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpBuildEndpoint();
        boolean success = jenkins.buildJob(JOB_NAME, TOKEN_VALUE);
        assertTrue(success);
    }

    @Test(expected = JenkinsManagerException.class)
    public void buildJobFailRequest() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpBuildEndpointFail();
        jenkins.buildJob(JOB_NAME, TOKEN_VALUE);
    }

    @Test(expected = JenkinsManagerException.class)
    public void buildJobNameNull() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        jenkins.buildJob(null, TOKEN_VALUE);
    }

    @Test(expected = JenkinsManagerException.class)
    public void buildJobNameEmpty() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        jenkins.buildJob("", TOKEN_VALUE);
    }

    @Test(expected = JenkinsManagerException.class)
    public void buildJobTokenNull() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        jenkins.buildJob(JOB_NAME, null);
    }

    @Test
    public void buildJobTokenEmpty() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpBuildEndpoint();
        boolean success = jenkins.buildJob(JOB_NAME, "");
        assertTrue(success);
    }

    @Test
    public void buildJobWithFormPostParams() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpBuildEndpointWithBody();
        boolean success = jenkins.buildJobWithFormPostParams(JOB_NAME, TOKEN_VALUE, BODY_JSON);
        assertTrue(success);
    }

    @Test
    public void buildJobWithParameters() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpBuildEndpointWithParameters();
        boolean success = jenkins.buildJobWithParameters(JOB_NAME, TOKEN_VALUE, parametersMap);
        assertTrue(success);
    }

    @Test
    public void getJenkinsBuildStatusData() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpStatusEndpoint();
        JSONObject json = jenkins.getJenkinsBuildStatusData(JOB_NAME, BUILD_NUMBER);
        assertEquals(BODY, json.toString());
    }

    @Test(expected = JenkinsManagerException.class)
    public void getJenkinsBuildStatusDataJobNameNull() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpStatusEndpoint();
        jenkins.getJenkinsBuildStatusData(null, BUILD_NUMBER);
    }

    @Test(expected = JenkinsManagerException.class)
    public void getJenkinsBuildStatusDataJobNameEmpty() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpStatusEndpoint();
        jenkins.getJenkinsBuildStatusData("", BUILD_NUMBER);
    }

    @Test
    public void getJenkinsBuildStatusDataBuildNumberNull() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpStatusEndpointFallback();
        JSONObject json = jenkins.getJenkinsBuildStatusData(JOB_NAME, null);
        assertEquals(BODY, json.toString());
    }

    @Test(expected = JenkinsManagerException.class)
    public void getJenkinsBuildStatusDataFailRequest() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpStatusEndpointFail();
        jenkins.getJenkinsBuildStatusData(JOB_NAME, BUILD_NUMBER);
    }

    @Test
    public void getJenkinsBuildStatusDataOnlyJobName() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpStatusEndpointFallback();
        JSONObject json = jenkins.getJenkinsBuildStatusData(JOB_NAME);
        assertEquals(BODY, json.toString());
    }

    @Test
    public void pluginExists() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpPluginEndpoint();
        boolean success = jenkins.pluginExists(PLUGIN_NAME);
        assertTrue(success);
    }

    @Test
    public void pluginExistsNotFound() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpPluginEndpoint();
        boolean success = jenkins.pluginExists(PLUGIN_NOT_FOUND);
        assertFalse(success);
    }

    @Test(expected = JenkinsManagerException.class)
    public void pluginExistsFailRequest() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpPluginEndpointFail();
        jenkins.pluginExists(PLUGIN_NAME);
    }

    @Test
    public void installPlugin() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpPluginInstallEndpoint();
        boolean success = jenkins.installPlugin(PLUGIN_NAME, PLUGIN_VERSION);
        assertTrue(success);
    }

    @Test(expected = JenkinsManagerException.class)
    public void installPluginNameNull() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpPluginInstallEndpoint();
        jenkins.installPlugin(null, PLUGIN_VERSION);
    }

    @Test(expected = JenkinsManagerException.class)
    public void installPluginNameEmpty() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpPluginInstallEndpoint();
        jenkins.installPlugin("", PLUGIN_VERSION);
    }

    @Test(expected = JenkinsManagerException.class)
    public void installPluginVersionNull() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpPluginInstallEndpoint();
        jenkins.installPlugin(PLUGIN_NAME, null);
    }

    @Test(expected = JenkinsManagerException.class)
    public void installPluginVersionEmpty() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpPluginInstallEndpoint();
        jenkins.installPlugin(PLUGIN_NAME, "");
    }

    @Test(expected = JenkinsManagerException.class)
    public void installPluginFailRequest() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpPluginInstallEndpointFail();
        jenkins.installPlugin(PLUGIN_NAME, PLUGIN_VERSION);
    }

    @Test
    public void restartJenkinsServerNeverDown() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpRestartAndAvailableEndpoint();
        // Returns true if jenkins is down on the first check and then later becomes available. Returns
        // false if jenkins never responded or if it is available on the first check.
        boolean success = jenkins.restartJenkins();
        assertFalse(success);
    }

    @Test(expected = JenkinsManagerException.class)
    public void restartJenkinsFailRequest() throws Exception {
        JenkinsManager jenkins = setUpJenkinsManager();
        setUpRestartEndpointFail();
        jenkins.restartJenkins();
    }

    private void setUpCrumbEndpoint() {
        mockServer.reset();
        mockServer.when(request().withMethod("GET").withPath(ENDPOINT_CRUMB))
                  .respond(response().withStatusCode(200).withBody(RESPONSE_CRUMB));
    }

    private void setUpFailCrumbEndpoint() {
        mockServer.reset();
        mockServer.when(request().withMethod("GET").withPath(ENDPOINT_CRUMB))
                  .respond(response().withStatusCode(500));
    }

    private void setUpCreateEndpoint() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_CREATE)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(200));
    }

    private void setUpCreateEndpointFail() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_CREATE)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(500));
    }

    private void setUpDeleteEndpoint() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_DELETE)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(302));
    }

    private void setUpDeleteEndpointFail() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_DELETE)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(500));
    }

    private void setUpDeleteAndCreateEndpoint() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_DELETE)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(302));
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_CREATE)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(200));
    }

    private void setUpBuildEndpoint() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("GET")
                                 .withPath(ENDPOINT_BUILD)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(201));
    }

    private void setUpBuildEndpointFail() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("GET")
                                 .withPath(ENDPOINT_BUILD)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(500));
    }

    private void setUpBuildEndpointWithBody() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_BUILD)
                                 .withHeader(HEADER_AUTH, encodedPassword)
                                 .withBody(BODY_JSON))
                  .respond(response().withStatusCode(201));
    }

    private void setUpBuildEndpointWithParameters() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter(PARAMETER_KEY, PARAMETER_VALUE));
        parameters.add(new Parameter(TOKEN_KEY, TOKEN_VALUE));
        mockServer.when(request().withMethod("GET")
                                 .withPath(ENDPOINT_BUILD_PARAMETERS)
                                 .withHeader(HEADER_AUTH, encodedPassword)
                                 .withQueryStringParameters(parameters))
                  .respond(response().withStatusCode(201));
    }

    private void setUpStatusEndpoint() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("GET")
                                 .withPath(ENDPOINT_STATUS)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(200).withBody(BODY));
    }

    private void setUpStatusEndpointFallback() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("GET")
                                 .withPath(ENDPOINT_STATUS_FALLBACK)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(200).withBody(BODY));
    }

    private void setUpStatusEndpointFail() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("GET")
                                 .withPath(ENDPOINT_STATUS)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(500));
    }

    private void setUpPluginEndpoint() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("depth", "1"));
        parameters.add(new Parameter("wrapper", "plugins"));
        mockServer.when(request().withMethod("GET")
                                 .withPath(ENDPOINT_PLUGIN)
                                 .withHeader(HEADER_AUTH, encodedPassword)
                                 .withQueryStringParameters(parameters))
                  .respond(response().withStatusCode(200).withBody(BODY_PLUGIN));
    }

    private void setUpPluginEndpointFail() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("depth", "1"));
        parameters.add(new Parameter("wrapper", "plugins"));
        mockServer.when(request().withMethod("GET")
                                 .withPath(ENDPOINT_PLUGIN)
                                 .withHeader(HEADER_AUTH, encodedPassword)
                                 .withQueryStringParameters(parameters))
                  .respond(response().withStatusCode(500));
    }

    private void setUpPluginInstallEndpoint() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_PLUGIN_INSTALL)
                                 .withHeader(HEADER_AUTH, encodedPassword)
                                 .withBody(BODY_PLUGIN_INSTALL))
                  .respond(response().withStatusCode(302));
    }

    private void setUpPluginInstallEndpointFail() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_PLUGIN_INSTALL)
                                 .withHeader(HEADER_AUTH, encodedPassword)
                                 .withBody(BODY_PLUGIN_INSTALL))
                  .respond(response().withStatusCode(500));
    }

    private void setUpRestartAndAvailableEndpoint() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_RESTART)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(302));
        mockServer.when(request().withMethod("GET")
                                 .withPath(ENDPOINT_JENKINS)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(200));
    }

    private void setUpRestartEndpointFail() throws UnsupportedEncodingException {
        mockServer.reset();
        String encodedPassword = "Basic "
                + createEncodingFromUsernameAndPassword(USERNAME, PASSWORD);
        mockServer.when(request().withMethod("POST")
                                 .withPath(ENDPOINT_RESTART)
                                 .withHeader(HEADER_AUTH, encodedPassword))
                  .respond(response().withStatusCode(500));
    }

    private String createEncodingFromUsernameAndPassword(String username, String password)
            throws UnsupportedEncodingException {
        String authString = String.join(":", username, password);
        String encoding = DatatypeConverter.printBase64Binary(authString.getBytes("utf-8"));
        return encoding;
    }

    private JenkinsManager setUpJenkinsManager()
            throws ClientProtocolException, URISyntaxException, IOException {
        setUpCrumbEndpoint();
        return new JenkinsManager(URL + ":" + port, USERNAME, PASSWORD);
    }

    private static int findOpenPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
