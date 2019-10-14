package com.ericsson.eiffelcommons.Utilstest;

import static org.junit.Assert.assertEquals;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.ericsson.eiffelcommons.utils.HttpRequest;
import com.ericsson.eiffelcommons.utils.HttpRequest.HttpMethod;

public class HttpRequestTest {
    private static final String EXPECTED_URI = "http://something.com/testing/test/";
    private static final String EXPECTED_HEADER = "Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=";

    @Test
    public void testBuildingOfURI() throws Exception {

        HttpRequest request = new HttpRequest(HttpMethod.POST);

        request.setBaseUrl("http://something.com");
        request.setEndpoint("/testing/test/");
        URIBuilder builder = (URIBuilder) Whitebox.invokeMethod(request, "createURIBuilder");
        assertEquals(EXPECTED_URI, builder.toString());

        request.setBaseUrl("http://something.com/");
        request.setEndpoint("/testing/test/");
        builder = (URIBuilder) Whitebox.invokeMethod(request, "createURIBuilder");
        assertEquals(EXPECTED_URI, builder.toString());

        request.setBaseUrl("http://something.com/");
        request.setEndpoint("testing/test/");
        builder = (URIBuilder) Whitebox.invokeMethod(request, "createURIBuilder");
        assertEquals(EXPECTED_URI, builder.toString());

        request.setBaseUrl("http://something.com");
        request.setEndpoint("testing/test/");
        builder = (URIBuilder) Whitebox.invokeMethod(request, "createURIBuilder");
        assertEquals(EXPECTED_URI, builder.toString());

        request.setBasicAuth("username", "password");
        HttpRequestBase client = Whitebox.getInternalState(request, "request");
        String actualAuthHeader = client.getAllHeaders()[0].toString();
        assertEquals(EXPECTED_HEADER, actualAuthHeader);
    }
}
