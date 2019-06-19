package com.ericsson.eiffelcommons.helpers;

public final class RegExProvider {

    public static final String SUBSCRIPTION_NAME = "(\\W)";
    public static final String NOTIFICATION_META = "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

    private RegExProvider() {
        throw new AssertionError();
    }
}
