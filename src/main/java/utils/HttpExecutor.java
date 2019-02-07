package utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public final class HttpExecutor {

    private static HttpExecutor instance;
    private CloseableHttpClient client = HttpClientBuilder.create().build();

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
    public void recreateHttpClient() throws IOException {
        if (this.client != null) {
            this.client.close();

        }
        this.client = HttpClientBuilder.create().build();
    }

    /**
     * Handle the response from a HTTP request
     * @param request
     *      A HTTP request method, e.g. httpGet, httpPost
     * @return ResponseEntity
     *      containing the json content of the http response and status code from request
     * @throws IOException
     * @throws ClientProtocolException
     * */
    public ResponseEntity executeRequest(HttpRequestBase request) throws ClientProtocolException, IOException {
        HttpResponse httpResponse = null;

        httpResponse = client.execute(request);
        return new ResponseEntity(httpResponse);


    }
}