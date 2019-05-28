package com.ericsson.eiffelcommons.Utilstest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Test;

import com.ericsson.eiffelcommons.utils.RegExProvider;

public class RegExProviderTest {

    @Test
    public void test() {
        String regExForInvalidName = getValue("invalidName");
        String regExForValidEmail = getValue("validEmail");

        assertEquals(regExForInvalidName, "(\\W)");
        assertEquals(regExForValidEmail,
                "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");
    }

    /**
     * This function takes a key and returns the corresponding value from the
     * JSONObject
     *
     * @param String
     *            key
     * @return String value
     */
    public String getValue(String key) {
        String value = null;
        try {
            value = (String) RegExProvider.getRegExs().get(key);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return value;
    }
}
