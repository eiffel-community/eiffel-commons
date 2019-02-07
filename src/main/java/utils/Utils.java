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
package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {

    /**
     * This function reads a file and returns the content as a JSONObject
     *
     * @param filepath
     * @return
     * @throws IOException
     */
    public static JSONArray getJsonArrayFromFile(String filepath) throws IOException {
        return new JSONArray(getStringFromFile(filepath));
    }

    /**
     * This function reads a file and returns the content as a JSONObject
     *
     * @param filepath
     * @return
     * @throws IOException
     */
    public static JSONObject getJsonObjectFromFile(String filepath) throws IOException {
        return new JSONObject(getStringFromFile(filepath));
    }

    /**
     * This function reads a file and returns the content as a String
     *
     * @param filepath
     * @return
     * @throws IOException
     */
    public static String getStringFromFile(String filepath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filepath)), StandardCharsets.UTF_8);
    }
}
