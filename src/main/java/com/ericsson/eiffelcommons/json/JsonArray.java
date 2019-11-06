package com.ericsson.eiffelcommons.json;

public class JsonArray {

    private org.json.JSONArray jsonArray;

    public JsonArray(String value) {
        this.jsonArray = new org.json.JSONArray(value);
    }

    public JsonObject getAsJsonObject(int index) {
        String value = jsonArray.getString(index);
        return new JsonObject(value);
    }

    @Override
    public String toString() {
        return jsonArray.toString();
    }

    public JsonArray put(int index, Object value) {
        jsonArray.put(index, value);
        return this;
    }

    public int length() {
        return jsonArray.length();
    }

    public JsonArray add(Object value) {
        jsonArray.put(value);
        return this;
    }
}
