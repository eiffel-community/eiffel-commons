package com.ericsson.eiffelcommons.httptest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.powermock.reflect.Whitebox;

import com.ericsson.eiffelcommons.http.HttpExecutor;
import com.ericsson.eiffelcommons.http.ResponseEntity;

public class HttpExecutorTest {

    private static final String URL_FAKE = "http://ensure-non-existant-webpage.com";
    private static final String URL_MOCK = "http://localhost:{port}";
    private static final String ENDPOINT_MOCK = "/endpoint";
    private static final String RESPONSE_MOCK = "{\"message\":\"dummy\"}";

    private static ClientAndServer clientAndServer;

    @BeforeClass
    public static void setUpMocks() throws IOException {
        clientAndServer = startClientAndServer();
    }

    @Test(expected = UnknownHostException.class)
    public void testDefaultConstructor() throws IOException, URISyntaxException {
        HttpGet request = new HttpGet();
        request.setURI(new URI(URL_FAKE));

        HttpExecutor executor = new HttpExecutor();
        executor.executeRequest(request);
    }

    @Test(expected = IllegalStateException.class)
    public void testHttpClientConstructor() throws IOException, URISyntaxException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        client.close();

        HttpGet request = new HttpGet();
        request.setURI(new URI(URL_FAKE));

        HttpExecutor executor = new HttpExecutor(client);
        executor.executeRequest(request);
    }

    @Test
    public void testGetInstance() throws IOException, URISyntaxException {
        HttpGet request = new HttpGet();
        request.setURI(new URI(URL_FAKE));

        HttpExecutor executor = new HttpExecutor();
        HttpExecutor instance = Whitebox.getInternalState(executor, "instance", HttpExecutor.class);
        assertNull(instance);

        // Test if instance null branch
        executor = HttpExecutor.getInstance();
        instance = Whitebox.getInternalState(executor, "instance", HttpExecutor.class);
        assertNotNull(instance);

        // Test if instance not null branch
        executor = HttpExecutor.getInstance();
        instance = Whitebox.getInternalState(executor, "instance", HttpExecutor.class);
        assertNotNull(instance);

        // Reset static instance
        instance = null;
        Whitebox.setInternalState(HttpExecutor.class, "instance", instance);
    }

    @Test
    public void testExecuteRequest()
            throws URISyntaxException, ClientProtocolException, IOException {
        setUpMock();

        HttpGet request = new HttpGet();
        String url = URL_MOCK.replace("{port}", String.valueOf(clientAndServer.getLocalPort()));
        URI uri = new URI(url + ENDPOINT_MOCK);
        request.setURI(uri);

        HttpExecutor executor = new HttpExecutor();
        ResponseEntity response = executor.executeRequest(request);
        assertEquals(RESPONSE_MOCK, response.getBody());
    }

    private void setUpMock() {
        clientAndServer.when(request().withMethod("GET").withPath(ENDPOINT_MOCK))
                       .respond(response().withStatusCode(200).withBody(RESPONSE_MOCK));
    }
}
