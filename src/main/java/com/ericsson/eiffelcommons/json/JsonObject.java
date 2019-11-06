package com.ericsson.eiffelcommons.json;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

public class JsonObject {

    public static Object NULL = org.json.JSONObject.NULL;

    private org.json.JSONObject jsonObject;

    public JsonObject() {
        this.jsonObject = new org.json.JSONObject();
    }

    public JsonObject(String json) {
        this.jsonObject = new org.json.JSONObject(json);
    }

    public JsonObject(Map<?, ?> map) {
        this.jsonObject = new org.json.JSONObject(map);
    }

    @Override
    public String toString() {
        return this.jsonObject.toString();
    }

    public String getAsString(String key) {
        return jsonObject.getString(key);
    }

    public int getAsInt(String key) {
        return jsonObject.getInt(key);
    }

    public boolean getAsBoolean(String key) {
        return jsonObject.getBoolean(key);
    }

    public JsonObject getAsJsonObject(String key) {
        String value = getAsString(key);
        return new JsonObject(value);
    }

    public JsonArray getAsJsonArray(String key) {
        String value = getAsString(key);
        return new JsonArray(value);
    }

    public JsonObject put(String key, Object value) {
        jsonObject.put(key, value);
        return this;
    }

    public Object remove(String key) {
        return jsonObject.remove(key);
    }

    @Deprecated
    public Iterator<String> keyIterator() {
        return jsonObject.keys();
    }

    public String[] keys() {
        return JSONObject.getNames(jsonObject);
    }

    public boolean has(String key) {
        return jsonObject.has(key);
    }

}
