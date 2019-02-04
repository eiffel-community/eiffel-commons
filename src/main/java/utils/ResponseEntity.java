package utils;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import lombok.Getter;

@Getter
public class ResponseEntity {

    private int statusCode;
    private String body;
    private Header[] headers;

    /**
     * Creates a ResponseEntity from a HttpResponse as input.
     * @param httpResponse
     * @throws ParseException
     * @throws IOException
     */
    public ResponseEntity(HttpResponse httpResponse) throws ParseException, IOException {
        this.statusCode = httpResponse.getStatusLine().getStatusCode();
        this.body = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
        this.headers = httpResponse.getAllHeaders();
    }

    /**
     * Creates a ResponseEntity with given parameters.
     * @param statusCode
     * @param body
     * @param headers
     */
    public ResponseEntity(int statusCode, String body, Header[] headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    public String getStatusCodeValue() {
        return String.valueOf(statusCode);
    }
}
