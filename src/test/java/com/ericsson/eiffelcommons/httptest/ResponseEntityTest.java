package com.ericsson.eiffelcommons.httptest;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.eiffelcommons.http.ResponseEntity;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EntityUtils.class })
public class ResponseEntityTest {

    private static final int STATUS_CODE = 200;
    private static final String CHARSET = "UTF-8";
    private static final String BODY = "{\"message\":\"dummy\"}";
    private static final Header[] HEADERS = new Header[0];

    private HttpResponse httpResponse;
    private StatusLine statusLine;
    private HttpEntity entity;

    @Test
    public void testResponseEntityConstructor() throws ParseException, IOException {
        setUpMockObjects();

        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(STATUS_CODE);
        when(httpResponse.getEntity()).thenReturn(entity);
        when(EntityUtils.toString(entity, CHARSET)).thenReturn(BODY);
        when(httpResponse.getAllHeaders()).thenReturn(HEADERS);

        ResponseEntity responseEntity = new ResponseEntity(httpResponse);
        assertEquals(STATUS_CODE, responseEntity.getStatusCode());
    }

    @Test
    public void testResponseEntityConstructorNullEntity() throws ParseException, IOException {
        setUpMockObjects();
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(STATUS_CODE);
        when(httpResponse.getEntity()).thenReturn(null);
        when(httpResponse.getAllHeaders()).thenReturn(new Header[0]);

        ResponseEntity responseEntity = new ResponseEntity(httpResponse);
        assertEquals(STATUS_CODE, responseEntity.getStatusCode());
    }

    @Test
    public void testResponseEntityValuesConstructor() throws ParseException, IOException {
        ResponseEntity responseEntity = new ResponseEntity(STATUS_CODE, BODY, HEADERS);
        assertEquals(String.valueOf(STATUS_CODE), responseEntity.getStatusCodeValue());
    }

    private void setUpMockObjects() {
        httpResponse = Mockito.mock(HttpResponse.class);
        statusLine = Mockito.mock(StatusLine.class);
        entity = Mockito.mock(HttpEntity.class);

        mockStatic(EntityUtils.class);
    }
}
