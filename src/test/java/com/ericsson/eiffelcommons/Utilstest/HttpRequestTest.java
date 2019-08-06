package com.ericsson.eiffelcommons.Utilstest;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;

import com.ericsson.eiffelcommons.utils.HttpRequest;
import com.ericsson.eiffelcommons.utils.HttpRequest.HttpMethod;

public class HttpRequestTest {
    @Test
    public void testBuildingOfURI() throws NoSuchFieldException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        String expectedURI = "http://something.com/testing/test/";
        HttpRequest request= new HttpRequest(HttpMethod.POST);
        Method method = request.getClass().getDeclaredMethod("createURIBuilder");
        method.setAccessible(true);

        request.setBaseUrl("http://something.com");
        request.setEndpoint("/testing/test/");
        URIBuilder builder = (URIBuilder) method.invoke(request);
        assertEquals(expectedURI, builder.toString());

        request.setBaseUrl("http://something.com/");
        request.setEndpoint("/testing/test/");
        builder = (URIBuilder) method.invoke(request);
        assertEquals(expectedURI, builder.toString());

        request.setBaseUrl("http://something.com/");
        request.setEndpoint("testing/test/");
        builder = (URIBuilder) method.invoke(request);
        assertEquals(expectedURI, builder.toString());

        request.setBaseUrl("http://something.com");
        request.setEndpoint("testing/test/");
        builder = (URIBuilder) method.invoke(request);
        assertEquals(expectedURI, builder.toString());
    }
}
