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
package com.ericsson.eiffelcommons.http;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

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
    protected ArrayList<NameValuePair> params;

    public HttpRequest() {
        params = new ArrayList<NameValuePair>();
        initExecutor(false);
    }

    public HttpRequest(HttpMethod method) {
        this(method, false);
    }

    public HttpRequest(HttpMethod method, HttpExecutor executor) {
        this(method, false);
        this.executor = executor;
    }

    public HttpRequest(HttpMethod method, boolean persistentClient) {
        params = new ArrayList<NameValuePair>();
        setHttpMethod(method);
        initExecutor(persistentClient);
    }

    private void initExecutor(boolean persistentClient) {
        if (persistentClient) {
            executor = HttpExecutor.getInstance();
        } else {
            executor = new HttpExecutor();
        }
    }

    /**
     * Sets the http method for this request object
     *
     * @param method
     */
    public HttpRequest setHttpMethod(HttpMethod method) {
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

        return this;
    }

    /**
     * Gets the base url(not including endpoint) for example: http://localhost:8080
     *
     * @return String
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base url(not including endpoint) for example: http://localhost:8080
     *
     * @param baseUrl
     */
    public HttpRequest setBaseUrl(String baseUrl) {
        baseUrl = trimBaseUrl(baseUrl);
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
     * @param key   :: the key of the header
     * @param value :: the value of the header
     * @return HttpRequest
     */
    public HttpRequest addHeader(String key, String value) {
        request.addHeader(key, value);
        return this;
    }

    /**
     * Function that overwrites the first header with the same name. The new header will be appended
     * to the end of the list, if no header with the given name can be found.
     *
     * @param key
     * @param value
     * @return HttpRequest
     */
    public HttpRequest setHeader(String key, String value) {
        Header header = new BasicHeader(key, value);
        request.setHeader(header);
        return this;
    }

    /**
     * Takes a header key as input and removes that key and value from the list of headers.
     *
     * @param headerKey :: the header to remove
     */
    public void removeHeader(String headerKey) {
        request.removeHeaders(headerKey);
    }

    /**
     * Function that adds multiple parameters to the http request.
     *
     * @param parameters :: List<NameValuePair>
     */
    public void addParameters(Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            addParameter(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Function that adds a parameter to the http request.
     *
     * @param key   :: the key of the parameter
     * @param value :: the value of the parameter
     * @return HttpRequest
     */
    public HttpRequest addParameter(String key, String value) {
        params.add(new BasicNameValuePair(key, value));
        return this;
    }

    /**
     * Function that sets the body of the http request with a chosen content type.
     *
     * @param body :: String input
     * @return HTTPRequest
     */
    public HttpRequest setBody(String body, ContentType contentType) {
        ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(body, contentType));
        return this;
    }

    /**
     * Function that sets the body of the http request with default content type(text/plain).
     *
     * @param body :: String input
     * @return HTTPRequest
     */
    public HttpRequest setBody(String body) {
        setBody(body, ContentType.TEXT_PLAIN);
        return this;
    }

    /**
     * Function that sets the body of the http request with default value of content type
     * (text/plain).
     *
     * @param file :: File input
     * @return HTTPRequest
     * @throws IOException
     */
    public HttpRequest setBody(File file) throws IOException {
        setBody(file, ContentType.TEXT_PLAIN);
        return this;
    }

    /**
     * Function that sets the body of the http request with a chosen content type.
     *
     * @param file :: File input
     * @param type
     * @return HTTPRequest
     * @throws IOException
     */
    public HttpRequest setBody(File file, ContentType contentType) throws IOException {
        String fileContent = "";
        try {
            fileContent = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            final String message = "Failed to read the Request body file:" + file.getPath()
                    + ". Message: "
                    + e.getMessage();
            throw new IOException(message);
        }
        return setBody(fileContent, contentType);
    }

    /**
     * Function that sets the Authorization header of the http request.
     *
     * @param username
     * @param password
     * @return
     * @throws UnsupportedEncodingException
     */
    public HttpRequest setBasicAuth(String username, String password)
            throws UnsupportedEncodingException {
        String auth = String.format("%s:%s", username, password);
        String encodedAuth = new String(Base64.encodeBase64(auth.getBytes()), "UTF-8");
        addHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
        return this;
    }

    /**
     * Function that executes the http request.
     *
     * @return ResponseEntity, the response of the performed http
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public ResponseEntity performRequest()
            throws URISyntaxException, ClientProtocolException, IOException {
        URI uri = createURI();
        uri = addParametersToURI(uri);
        request.setURI(uri);
        return executor.executeRequest(request);
    }

    /**
     * Function that returns the URI of the request.
     *
     * @return URI
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    @Deprecated
    public URI getURI() throws MalformedURLException, URISyntaxException {
        return createURI();
    }

    /**
     * Function that creates the URI from the baseUrl and endpoint
     *
     * @param
     * @return URI
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public URI createURI() throws MalformedURLException, URISyntaxException {
        if (!StringUtils.isEmpty(endpoint) && endpoint.startsWith("/")) {
            return new URL(baseUrl + endpoint).toURI();
        } else if (!StringUtils.isEmpty(endpoint)) {
            return new URL(baseUrl + "/" + endpoint).toURI();
        } else {
            return new URL(baseUrl).toURI();
        }

    }

    /**
     * Function that adds parameters to the URI
     *
     * @param oldUri
     * @return URI
     * @throws URISyntaxException
     */
    private URI addParametersToURI(URI oldUri) throws URISyntaxException {
        String query = URLEncodedUtils.format(params, Consts.UTF_8);
        URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), query,
                oldUri.getFragment());
        return newUri;
    }

    /**
     * Function that trims the base url for trailing slashes
     *
     * @return HttpRequest
     */
    private String trimBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl;
    }
}