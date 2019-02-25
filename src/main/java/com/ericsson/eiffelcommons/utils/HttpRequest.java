/*
   Copyright 2019 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.eiffelcommons.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class HttpRequest {

    private HttpRequestBase request;
    private HttpExecutor executor;

    public enum HttpMethod {
        GET, POST, DELETE, PUT
    }

    protected String baseUrl;

    @Getter
    @Setter
    protected String endpoint;

    @Getter
    protected Map<String, String> params;

    public HttpRequest(HttpMethod method) {
        this(method, false);
    }

    public HttpRequest(HttpMethod method, boolean peristantClient) {
        params = new HashMap<>();

        switch (method) {
            case POST:
                request = new HttpPost();
                break;
            case GET:
                request = new HttpGet();
                break;
            case DELETE:
                request = new HttpDelete();
                break;
            case PUT:
                request = new HttpPut();
                break;
        }

        initExecutor(peristantClient);
    }

    private void initExecutor(boolean peristantClient) {
        if(peristantClient) {
            executor = HttpExecutor.getInstance();
        } else {
            executor = new HttpExecutor();
        }
    }

    /**
     * Gets the base url(not including endpoint) for example: http://localhost:8080
     * @return String
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base url(not including endpoint) for example: http://localhost:8080
     * @param baseUrl
     */
    public HttpRequest setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Function that cleans the parameters field.
     */
    public void cleanParams() {
        params.clear();
    }

    /**
     * Function that resets the HTTP Request object so it can be reused.
     */
    public void resetHttpRequestObject() {
        this.cleanParams();
        request.reset();
    }

    /**
     * Function the adds a header to the http request.
     *
     * @param key
     *            :: the key of the header
     * @param value
     *            :: the value of the header
     * @return
     */
    public HttpRequest addHeader(String key, String value) {
        request.addHeader(key, value);
        return this;
    }

    /**
     * Takes a header key as input and removes that key and value from the list of headers.
     *
     * @param headerKey
     *            :: the header to remove
     */
    public void removeHeader(String headerKey) {
        request.removeHeaders(headerKey);
    }

    /**
     * Function that adds multiple parameters to the http request.
     *
     * @param parameters
     *            :: List<NameValuePair>
     */
    public void addParameters(Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            addParam(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Function that adds a parameter to the http request.
     *
     * @param key
     *            :: the key of the parameter
     * @param value
     *            :: the value of the parameter
     * @return
     */
    public HttpRequest addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    /**
     * Function that sets the body of the http request.
     *
     * @param body
     *            :: String input
     * @return HTTPRequest
     */
    public HttpRequest setBody(String body) {
        ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(body, "UTF-8"));
        return this;
    }

    /**
     * Function that sets the body of the http request.
     *
     * @param file
     *            :: File input
     * @return HTTPRequest
     * @throws IOException
     */
    public HttpRequest setBody(File file) throws IOException {
        String fileContent = "";
        try {
            fileContent = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            final String message = "Failed to read the Request body file:" + file.getPath() + ". Message: "
                    + e.getMessage();
            throw new IOException(message);
        }
        return setBody(fileContent);
    }

    /**
     * Function that executes the http request.
     *
     * @return ResponseEntity, the response of the performed http
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public ResponseEntity performRequest() throws URISyntaxException, ClientProtocolException, IOException {
        URIBuilder builder = new URIBuilder(baseUrl + endpoint);

        if (!params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        request.setURI(builder.build());
        return executor.executeRequest(request);
    }
}