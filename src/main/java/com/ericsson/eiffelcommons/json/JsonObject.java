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

import java.util.Iterator;
import java.util.Map;

public class JsonObject {

    public static Object NULL = org.json.JSONObject.NULL;

    private org.json.JSONObject jsonObject;

    /**
     * Construct an empty JsonObject.
     */
    public JsonObject() {
        this.jsonObject = new org.json.JSONObject();
    }

    /**
     * Construct a JsonObject from a JSON string
     * 
     * @param json
     */
    public JsonObject(String json) {
        this.jsonObject = new org.json.JSONObject(json);
    }

    /**
     * Construct a JsonObject from a Map.
     * 
     * @param map
     */
    public JsonObject(Map<?, ?> map) {
        this.jsonObject = new org.json.JSONObject(map);
    }

    @Override
    public String toString() {
        return this.jsonObject.toString();
    }

    /**
     * Get the string associated with the key.
     * 
     * @param key
     * @return String
     */
    public String getAsString(String key) {
        return jsonObject.getString(key);
    }

    /**
     * Get the int associated with the key.
     * 
     * @param key
     * @return int
     */
    public int getAsInt(String key) {
        return jsonObject.getInt(key);
    }

    /**
     * Get the boolean associated with the key.
     * 
     * @param key
     * @return boolean
     */
    public boolean getAsBoolean(String key) {
        return jsonObject.getBoolean(key);
    }

    /**
     * Get the value associated with the key as JsonObject.
     * 
     * @param key
     * @return JsonObject
     */
    public JsonObject getAsJsonObject(String key) {
        String value = getAsString(key);
        return new JsonObject(value);
    }

    /**
     * Get the value associated with the key as JsonArray.
     * 
     * @param key
     * @return JsonArray
     */
    public JsonArray getAsJsonArray(String key) {
        String value = getAsString(key);
        return new JsonArray(value);
    }

    /**
     * Put a key/value pair in the JsonObject. If the value is null, then the key will be removed
     * from the JsonObject if it is present.
     * 
     * @param key
     * @param value
     * @return JsonObject
     */
    public JsonObject put(String key, Object value) {
        jsonObject.put(key, value);
        return this;
    }

    /**
     * Remove a key/value pair from the JsonObject, if present.
     * 
     * @param key
     * @return
     */
    public Object remove(String key) {
        return jsonObject.remove(key);
    }

    /**
     * Get an enumeration of the keys of the JsonObject. Modifying this key Set will also modify the
     * JSONObject. Use with caution.
     * 
     * @return Iterator<String>
     */
    @Deprecated
    public Iterator<String> keyIterator() {
        return jsonObject.keys();
    }

    /**
     * Get an array of field names from a JsonObject.
     * @return
     */
    public String[] keys() {
        return org.json.JSONObject.getNames(jsonObject);
    }

    /**
     * Determine if the JsonObject contains a specific key.
     * @param key
     * @return boolean
     */
    public boolean has(String key) {
        return jsonObject.has(key);
    }
}
