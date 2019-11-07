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
package com.ericsson.eiffelcommons.json;

public class JsonArray {

    private org.json.JSONArray jsonArray;

    /**
     * Construct a JsonArray from a JSON string.
     * 
     * @param value
     */
    public JsonArray(String value) {
        this.jsonArray = new org.json.JSONArray(value);
    }

    /**
     * Get the object as the specific index as JsonObject.
     * 
     * @param index
     * @return JsonObject
     */
    public JsonObject getAsJsonObject(int index) {
        String value = jsonArray.getString(index);
        return new JsonObject(value);
    }

    @Override
    public String toString() {
        return jsonArray.toString();
    }

    /**
     * Put or replace an object value in the JsonArray. If the index is greater than the length of
     * the JSONArray, then null elements will be added as necessary to pad it out.
     * 
     * @param index
     * @param value
     * @return JsonArray
     */
    public JsonArray put(int index, Object value) {
        jsonArray.put(index, value);
        return this;
    }

    /**
     * Append an object value to the JsonArray. This increases the array's length by one.
     *
     * @param value
     * @return JsonArray
     */
    public JsonArray add(Object value) {
        jsonArray.put(value);
        return this;
    }

    /**
     * Get the number of elements in the JSONArray, included nulls.
     * 
     * @return int
     */
    public int length() {
        return jsonArray.length();
    }
}
