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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {

    /**
     * Return a resource file in string format.
     *
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    public static String getResourceFileAsString(String fileName) throws FileNotFoundException {
        InputStream inputStream = Utils.class.getResourceAsStream(fileName);
        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.lines()
                         .collect(Collectors.joining(System.lineSeparator()));
        }
        throw new FileNotFoundException("Could not locate recourse file [" + fileName + "].");
    }

    /**
     * This function reads a file and returns the content as a JSONObject
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static JSONArray getResourceFileAsJsonArray(String fileName) throws FileNotFoundException {
        return new JSONArray(getResourceFileAsString(fileName));
    }

    /**
     * This function reads a file and returns the content as a JSONObject
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static JSONObject getResourceFileAsJsonObject(String fileName) throws FileNotFoundException {
        return new JSONObject(getResourceFileAsString(fileName));
    }
}
