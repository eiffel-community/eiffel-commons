package com.ericsson.eiffelcommons.httptest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.ericsson.eiffelcommons.http.HttpRequest;
import com.ericsson.eiffelcommons.http.HttpRequest.HttpMethod;

public class HttpRequestTest {
    private static final String EXPECTED_URI = "http://something.com/testing/test/";
    private static final String EXPECTED_HEADER = "Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=";
    private static final String URL_1 = "http://something.com";
    private static final String PARAMETER_KEY_1 = "subscriptionName";
    private static final String PARAMETER_VALUE_1 = "myTestSubscription";
    private static final String PARAMETER_KEY_2 = "otherKey";
    private static final String PARAMETER_VALUE_2 = "otherValue";

    @Test
    public void testBuildingOfURI() throws Exception {

        HttpRequest request = new HttpRequest(HttpMethod.POST);

        request.setBaseUrl("http://something.com");
        request.setEndpoint("/testing/test/");
        URI uri = (URI) Whitebox.invokeMethod(request, "createURI");
        assertEquals(EXPECTED_URI, uri.toString());

        request.setBaseUrl("http://something.com/");
        request.setEndpoint("/testing/test/");
        uri = (URI) Whitebox.invokeMethod(request, "createURI");
        assertEquals(EXPECTED_URI, uri.toString());

        request.setBaseUrl("http://something.com/");
        request.setEndpoint("testing/test/");
        uri = (URI) Whitebox.invokeMethod(request, "createURI");
        assertEquals(EXPECTED_URI, uri.toString());

        request.setBaseUrl("http://something.com");
        request.setEndpoint("testing/test/");
        uri = (URI) Whitebox.invokeMethod(request, "createURI");
        assertEquals(EXPECTED_URI, uri.toString());

        request.setBasicAuth("username", "password");
        HttpRequestBase client = Whitebox.getInternalState(request, "request");
        String actualAuthHeader = client.getAllHeaders()[0].toString();
        assertEquals(EXPECTED_HEADER, actualAuthHeader);
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

    @Test(expected = UnknownHostException.class)
    public void testPerformRequestWithParameters()
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_1).addParameter(PARAMETER_KEY_1, PARAMETER_VALUE_1);
        request.performRequest();
    }
}
