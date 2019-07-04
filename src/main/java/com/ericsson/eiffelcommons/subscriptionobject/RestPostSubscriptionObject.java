package com.ericsson.eiffelcommons.subscriptionobject;

import java.io.IOException;

public class RestPostSubscriptionObject extends SubscriptionObject {

    /**
     * Creates a subscriptionObject with REST/POST capabilities.
     * @param subscriptionName
     * @throws IOException
     */
    public RestPostSubscriptionObject(String subscriptionName) throws IOException {
        super(subscriptionName);

        subscriptionJson.put("notificationType", "REST_POST");
    }

    /**
     * Sets the field authenticationType to BASIC_AUTH together with the username and password.
     * @param username
     * @param password
     */
    @Deprecated
    public void setBasicAuth(String username, String password) {
        setAuthenticationType("BASIC_AUTH");
        setUsername(username);
        setPassword(password);
    }

    /**
     * Sets the field authenticationType to given value
     * @param authenticationType
     * @return RestPostSubscriptionObject
     */
    public RestPostSubscriptionObject setAuthenticationType(String authenticationType) {
        subscriptionJson.put("authenticationType", authenticationType);
        return this;
    }

    /**
     * Sets the field username in a subscription.
     * @param username
     * @param password
     * @return RestPostSubscriptionObject
     */
    public RestPostSubscriptionObject setUsername(String username) {
        subscriptionJson.put("userName", username);
        return this;
    }

    /**
     * Sets the field password in a subscription.
     * @param username
     * @param password
     * @return RestPostSubscriptionObject
     */
    public RestPostSubscriptionObject setPassword(String password) {
        subscriptionJson.put("password", password);
        return this;
    }
}
