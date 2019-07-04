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
        setAuthenticationTypeUsernameAndPassword("BASIC_AUTH", username, password);
    }

    /**
     * Sets the field authenticationType together with the username and password.
     * @param authenticationType
     * @param username
     * @param password
     */
    public void setAuthenticationTypeUsernameAndPassword(String authenticationType, String username, String password) {
        setAuthenticationType(authenticationType);
        setUsernameAndPassword(username, password);
    }

    /**
     * Sets the field authenticationType to given value
     * @param authenticationType
     */
    public void setAuthenticationType(String authenticationType) {
        subscriptionJson.put("authenticationType", authenticationType);
    }

    /**
     * Sets the fields Username and Password in a subscription.
     * @param username
     * @param password
     */
    public void setUsernameAndPassword(String username, String password) {
        subscriptionJson.put("userName", username);
        subscriptionJson.put("password", password);
    }
}
