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
package com.ericsson.ec.utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public final class HttpExecutor {

    private static HttpExecutor instance;
    private CloseableHttpClient client = HttpClientBuilder.create()
                                                          .build();

    private HttpExecutor() {

    }

    public static HttpExecutor getInstance() {
        if (instance == null) {
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
        this.client = HttpClientBuilder.create()
                                       .build();
    }

    /**
     * Handle the response from a HTTP request
     *
     * @param request
     *            :: A HTTP request method, e.g. httpGet, httpPost
     * @return ResponseEntity containing the json content of the http response and status code from request
     * @throws IOException
     * @throws ClientProtocolException
     */
    public ResponseEntity executeRequest(HttpRequestBase request) throws ClientProtocolException, IOException {
        HttpResponse httpResponse = null;

        httpResponse = client.execute(request);
        return new ResponseEntity(httpResponse);
    }
}