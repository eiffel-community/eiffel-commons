package com.ericsson.eiffelcommons.utils;

import java.io.IOException;

import org.json.JSONObject;

public class RegExProvider {
    private static final String REGULAR_EXPRESSIONS_PATH = "regExs.json";

    /**
     * This method returns Json Object for regular expressions
     *
     * @return JSONObject
     */
    public static JSONObject getRegExs() throws IOException {

        return Utils.getResourceFileAsJsonObject(REGULAR_EXPRESSIONS_PATH);
    }
}
