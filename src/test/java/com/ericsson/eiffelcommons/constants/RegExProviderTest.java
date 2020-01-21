package com.ericsson.eiffelcommons.constants;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ericsson.eiffelcommons.constants.RegExProvider;

public class RegExProviderTest {

    @Test
    public void test() {

        String expectedRegexForSubscriptionName = "(\\W)";
        String expectedRegexForEmail = "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

        assertEquals(RegExProvider.SUBSCRIPTION_NAME, expectedRegexForSubscriptionName);
        assertEquals(RegExProvider.NOTIFICATION_META, expectedRegexForEmail);
    }
}
