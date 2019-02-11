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
     *
     * @param httpResponse
     * @throws ParseException
     * @throws IOException
     */
    public ResponseEntity(HttpResponse httpResponse) throws ParseException, IOException {
        this.statusCode = httpResponse.getStatusLine()
                                      .getStatusCode();
        this.body = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
        this.headers = httpResponse.getAllHeaders();
    }

    /**
     * Creates a ResponseEntity with given parameters.
     *
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
