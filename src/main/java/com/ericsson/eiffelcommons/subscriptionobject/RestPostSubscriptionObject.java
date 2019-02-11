package com.ericsson.eiffelcommons.subscriptionobject;

import java.io.IOException;

public class RestPostSubscriptionObject extends SubscriptionObject {

    public RestPostSubscriptionObject(String subscriptionName) throws IOException {
        super(subscriptionName);

        subscriptionJson.put("notificationType", "REST_POST");
    }

    /**
     * Sets the field authenticationType to BASIC_AUTH together with the username and password.
     * @param username
     * @param password
     */
    public void setBasicAuth(String username, String password) {
        subscriptionJson.put("userName", username);
        subscriptionJson.put("password", password);
        subscriptionJson.put("authenticationType", "BASIC_AUTH");
    }
}
