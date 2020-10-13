package com.ericsson.eiffelcommons.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.ericsson.eiffelcommons.http.HttpExecutor;
import com.ericsson.eiffelcommons.http.HttpRequest;
import com.ericsson.eiffelcommons.http.HttpRequest.HttpMethod;

public class HttpRequestTest {
    private static final String URL_1 = "http://something.com";
    private static final String URL_2 = "http://something.com/";
    private static final String URL_3 = "http://someothing.com";
    private static final String URL_BAD_PROTOCOL = "httpl://something.com/";
    private static final String URL_BAD_SYNTAX = "http:<<something.com/";
    private static final String ENDPOINT_1 = "/testing/test/";
    private static final String ENDPOINT_2 = "testing/test/";
    private static final String HEADER_KEY_1 = "Cache-Control";
    private static final String HEADER_VALUE_1 = "no-cache";
    private static final String HEADER_KEY_2 = "Authorization";
    private static final String HEADER_VALUE_2 = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
    private static final String PARAMETER_KEY_1 = "subscriptionName";
    private static final String PARAMETER_VALUE_1 = "myTestSubscription";
    private static final String PARAMETER_KEY_2 = "otherKey";
    private static final String PARAMETER_VALUE_2 = "otherValue";
    private static final String BODY_CONTENT = "{\"subscriptionName\":\"myTestSubscription\"}";
    private static final String BODY_CONTENT_FILE = "src/test/resources/body-content.json";
    private static final String BODY_CONTENT_FILE_INVALID = "src/test/resources/none.json";
    private static final String BODY_HEADER = "Content-Type: application/json";
    private static final String BODY_HEADER_DEFAULT = "Content-Type: text/plain";

    private static final String EXPECTED_URI = "http://something.com/testing/test/";
    private static final String EXPECTED_HEADER = "Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=";

    @Test
    public void testBuildingOfURI() throws Exception {

        HttpRequest request = new HttpRequest(HttpMethod.POST);

        request.setBaseUrl(URL_1);
        request.setEndpoint(ENDPOINT_1);
        URI uri = (URI) Whitebox.invokeMethod(request, "createURI");
        assertEquals(EXPECTED_URI, uri.toString());

        request.setBaseUrl(URL_2);
        request.setEndpoint(ENDPOINT_1);
        uri = (URI) Whitebox.invokeMethod(request, "createURI");
        assertEquals(EXPECTED_URI, uri.toString());

        request.setBaseUrl(URL_2);
        request.setEndpoint(ENDPOINT_2);
        uri = (URI) Whitebox.invokeMethod(request, "createURI");
        assertEquals(EXPECTED_URI, uri.toString());

        request.setBaseUrl(URL_1);
        request.setEndpoint(ENDPOINT_2);
        uri = (URI) Whitebox.invokeMethod(request, "createURI");
        assertEquals(EXPECTED_URI, uri.toString());
    }

    @Test
    public void testAuthHeader() throws UnsupportedEncodingException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBasicAuth("username", "password");
        HttpRequestBase client = Whitebox.getInternalState(request, "request");
        String actualAuthHeader = client.getAllHeaders()[0].toString();
        assertEquals(EXPECTED_HEADER, actualAuthHeader);
    }

    @Test
    public void testSetHttpMethod() {
        HttpRequest request = new HttpRequest();
        request.setHttpMethod(HttpMethod.GET);
        HttpRequestBase method = Whitebox.getInternalState(request, "request", HttpRequest.class);
        assertTrue(method instanceof HttpGet);
    }

    @Test
    public void testHttpExecutorConstructorAndPersistancy() {
        HttpExecutor executor = new HttpExecutor();
        new HttpRequest(HttpMethod.GET, executor);
        HttpExecutor instance = Whitebox.getInternalState(executor, "instance", HttpExecutor.class);
        assertNull(instance);

        new HttpRequest(HttpMethod.GET, true);
        instance = Whitebox.getInternalState(executor, "instance", HttpExecutor.class);
        assertNotNull(instance);

        // Reset static instance
        instance = null;
        Whitebox.setInternalState(HttpExecutor.class, "instance", instance);
    }

    @Test
    public void testGettersAndSetters() {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_1);
        assertEquals(URL_1, request.getBaseUrl());

        request.setEndpoint(ENDPOINT_1);
        assertEquals(ENDPOINT_1, request.getEndpoint());
    }

    @Test
    public void testBodyProperty() throws UnsupportedOperationException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.POST);
        request.setBody(BODY_CONTENT, ContentType.APPLICATION_JSON);
        HttpEntityEnclosingRequestBase requestBase = Whitebox.getInternalState(request, "request",
                HttpRequest.class);
        HttpEntity entity = requestBase.getEntity();
        String actualHeader = entity.getContentType().toString();
        InputStream stream = entity.getContent();
        String actualBody = IOUtils.toString(entity.getContent(), "UTF-8");
        stream.close();
        assertTrue(actualHeader.contains(BODY_HEADER));
        assertEquals(BODY_CONTENT, actualBody);

        request.setBody(BODY_CONTENT);
        requestBase = Whitebox.getInternalState(request, "request", HttpRequest.class);
        entity = requestBase.getEntity();
        actualHeader = entity.getContentType().toString();
        stream = entity.getContent();
        actualBody = IOUtils.toString(entity.getContent(), "UTF-8");
        stream.close();
        assertTrue(actualHeader.contains(BODY_HEADER_DEFAULT));
        assertEquals(BODY_CONTENT, actualBody);

        File file = new File(BODY_CONTENT_FILE);
        request.setBody(file);
        requestBase = Whitebox.getInternalState(request, "request", HttpRequest.class);
        entity = requestBase.getEntity();
        actualHeader = entity.getContentType().toString();
        stream = entity.getContent();
        actualBody = IOUtils.toString(entity.getContent(), "UTF-8");
        stream.close();
        assertTrue(actualHeader.contains(BODY_HEADER_DEFAULT));
        assertEquals(BODY_CONTENT, actualBody.replace(System.getProperty("line.separator"), ""));
    }

    @Test(expected = IOException.class)
    public void testBodyException() throws IOException {
        HttpRequest request = new HttpRequest(HttpMethod.POST);
        File file = new File(BODY_CONTENT_FILE_INVALID);
        request.setBody(file);
    }

    @Test
    public void testParameterProperty() {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        assertEquals(0, request.getParams().size());

        request.addParameter(PARAMETER_KEY_1, PARAMETER_VALUE_1);
        assertEquals(1, request.getParams().size());

        request.resetHttpRequestObject();
        assertEquals(0, request.getParams().size());

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(PARAMETER_KEY_1, PARAMETER_VALUE_1);
        parameters.put(PARAMETER_KEY_2, PARAMETER_VALUE_2);
        request.addParameters(parameters);
        assertEquals(2, request.getParams().size());
    }

    @Test
    public void testAddParametersToURI() throws Exception {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_1)
               .addParameter(PARAMETER_KEY_1, PARAMETER_VALUE_1)
               .addParameter(PARAMETER_KEY_2, PARAMETER_VALUE_2);
        URI uri = (URI) Whitebox.invokeMethod(request, "createURI");
        URI newUri = (URI) Whitebox.invokeMethod(request, "addParametersToURI", uri);
        String expectedURI = URL_1 + "?" + PARAMETER_KEY_1 + "=" + PARAMETER_VALUE_1 + "&"
                + PARAMETER_KEY_2 + "=" + PARAMETER_VALUE_2;
        assertEquals(expectedURI, newUri.toString());
    }

    public void testHeaderProperty() {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.addHeader(HEADER_KEY_1, HEADER_VALUE_1);
        request.setHeader(HEADER_KEY_2, HEADER_VALUE_2);
        HttpRequestBase method = Whitebox.getInternalState(request, "request", HttpRequest.class);
        String expectedHeader = HEADER_KEY_1 + ": " + HEADER_VALUE_1;
        String actualHeader = method.getAllHeaders()[0].toString();
        assertEquals(expectedHeader, actualHeader);

        expectedHeader = HEADER_KEY_2 + ": " + HEADER_VALUE_2;
        actualHeader = method.getAllHeaders()[1].toString();
        assertEquals(expectedHeader, actualHeader);

        request.removeHeader(HEADER_KEY_1);
        expectedHeader = HEADER_KEY_2 + ": " + HEADER_VALUE_2;
        actualHeader = method.getAllHeaders()[0].toString();
        assertEquals(expectedHeader, actualHeader);
    }

    @Test
    public void testGetURI() throws MalformedURLException, URISyntaxException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_1).setEndpoint(ENDPOINT_1);
        URI uri = request.createURI();
        String fullURI = uri.toString();
        assertEquals(URL_1 + ENDPOINT_1, fullURI);
    }

    @Test(expected = UnknownHostException.class)
    public void testPerformRequestUnknownHost()
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_3).setEndpoint(ENDPOINT_1);
        request.performRequest();
    }

    @Test(expected = MalformedURLException.class)
    public void testPerformRequestBadProtocol() throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_BAD_PROTOCOL);
        request.performRequest();
    }

    @Test(expected = URISyntaxException.class)
    public void testPerformRequestBadSyntax()
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_BAD_SYNTAX);
        request.performRequest();
    }

    @Test(expected = UnknownHostException.class)
    public void testPerformRequestNoEndpoint()
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_3);
        request.performRequest();
    }

    @Test(expected = UnknownHostException.class)
    public void testPerformRequestWithParameters()
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_3).addParameter(PARAMETER_KEY_1, PARAMETER_VALUE_1);
        request.performRequest();
    }
}
