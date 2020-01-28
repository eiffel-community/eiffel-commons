package com.ericsson.eiffelcommons.utils;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class FileUtilsTest {

    public static final String JSON_FILE = "body-content.json";
    public static final String JSON_ARRAY_FILE = "json-array.json";
    public static final String INVALID_FILE_PATH = "dummy.json";
    public static final String KEY_SUBSCRIPTION = "subscriptionName";
    public static final String VALUE_SUBSCRIPTION = "myTestSubscription";

    @Test
    public void getFileAsString() throws FileNotFoundException {
        String actualContent = FileUtils.getResourceFileAsString(JSON_FILE);
        assertTrue(actualContent.contains(VALUE_SUBSCRIPTION));
    }

    @Test(expected = FileNotFoundException.class)
    public void getFileAsStringNotFound() throws FileNotFoundException {
        FileUtils.getResourceFileAsString(INVALID_FILE_PATH);
    }

    @Test
    public void getFileAsJsonObject() throws FileNotFoundException {
        JSONObject json = FileUtils.getResourceFileAsJsonObject(JSON_FILE);
        String actualValue = json.getString(KEY_SUBSCRIPTION);
        assertTrue(actualValue.contains(VALUE_SUBSCRIPTION));
    }

    @Test(expected = FileNotFoundException.class)
    public void getFileAsJsonObjectNotFound() throws FileNotFoundException {
        FileUtils.getResourceFileAsJsonObject(INVALID_FILE_PATH);
    }

    @Test
    public void getFileAsJsonArray() throws FileNotFoundException {
        JSONArray json = FileUtils.getResourceFileAsJsonArray(JSON_ARRAY_FILE);
        String actualValue = json.getJSONObject(0).getString(KEY_SUBSCRIPTION);
        assertTrue(actualValue.contains(VALUE_SUBSCRIPTION));
    }

    @Test(expected = FileNotFoundException.class)
    public void getFileAsJsonArrayNotFound() throws FileNotFoundException {
        FileUtils.getResourceFileAsJsonArray(INVALID_FILE_PATH);
    }
}
