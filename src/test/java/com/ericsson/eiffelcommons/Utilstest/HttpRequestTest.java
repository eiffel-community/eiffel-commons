package com.ericsson.eiffelcommons.Utilstest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;

import com.ericsson.eiffelcommons.utils.HttpRequest;
import com.ericsson.eiffelcommons.utils.HttpRequest.HttpMethod;

public class HttpRequestTest {
    @Test
    public void testBuildingOfURI()
            throws NoSuchFieldException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            URISyntaxException, ClientProtocolException, IOException {
        String expectedURI = "http://something.com/testing/test/";
        String expectedAuthParam = "Authorization=Basic dXNlcm5hbWU6cGFzc3dvcmQ=";
        HttpRequest request = new HttpRequest(HttpMethod.POST);
        Method createURIBuilder = HttpRequest.class.getDeclaredMethod("createURIBuilder");
        Method addParametersToURIBuilder = HttpRequest.class.getDeclaredMethod(
                "addParametersToURIBuilder", URIBuilder.class);
        createURIBuilder.setAccessible(true);
        addParametersToURIBuilder.setAccessible(true);

        request.setBaseUrl("http://something.com");
        request.setEndpoint("/testing/test/");
        URIBuilder builder = (URIBuilder) createURIBuilder.invoke(request);
        assertEquals(expectedURI, builder.toString());

        request.setBaseUrl("http://something.com/");
        request.setEndpoint("/testing/test/");
        builder = (URIBuilder) createURIBuilder.invoke(request);
        assertEquals(expectedURI, builder.toString());

        request.setBaseUrl("http://something.com/");
        request.setEndpoint("testing/test/");
        builder = (URIBuilder) createURIBuilder.invoke(request);
        assertEquals(expectedURI, builder.toString());

        request.setBaseUrl("http://something.com");
        request.setEndpoint("testing/test/");
        builder = (URIBuilder) createURIBuilder.invoke(request);
        assertEquals(expectedURI, builder.toString());

        request.setBasicAuth("username", "password");
        builder = (URIBuilder) createURIBuilder.invoke(request);
        builder = (URIBuilder) addParametersToURIBuilder.invoke(request, builder);
        String actualAuthParam = builder.getQueryParams().get(0).toString();
        assertEquals(expectedAuthParam, actualAuthParam);
    }
}
