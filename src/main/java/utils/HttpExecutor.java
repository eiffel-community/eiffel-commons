package utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class HttpExecutor {

    private static HttpExecutor instance;
    private CloseableHttpClient client = HttpClientBuilder.create().build();
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpExecutor.class);

    private HttpExecutor() {

    }

    public static HttpExecutor getInstance() {
        if(instance == null) {
            instance = new HttpExecutor();
        }

        return instance;
    }

    /**
     * Close existing HttpClient and create a new one.
     *
     * @throws IOException
     */
    public void recreateHttpClient() {
        if (this.client != null) {
            try {
                this.client.close();
            } catch (IOException e) {
                LOGGER.error("Error: {}", e.getMessage());
            }
        }
        this.client = HttpClientBuilder.create().build();
    }

    /**
     * Handle the response from a HTTP request
     * @param request
     *      A HTTP request method, e.g. httpGet, httpPost
     * @return ResponseEntity
     *      containing the json content of the http response and status code from request
     * */
    public ResponseEntity executeRequest(HttpRequestBase request) {
        HttpResponse httpResponse = null;

        try {
            httpResponse = client.execute(request);
            return new ResponseEntity(httpResponse);
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;

    }
}