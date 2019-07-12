package com.ericsson.eiffelcommons.subscriptionobject;

import java.io.IOException;

public class RestPostSubscriptionObject extends SubscriptionObject<RestPostSubscriptionObject> {

    private final String NOTIFICATION_TYPE_KEY = "notificationType";
    private final String NOTIFICATION_TYPE = "REST_POST";

    private final String AUTHENTICATION_TYPE_KEY = "authenticationType";
    private final String USERNAME_KEY = "userName";
    private final String PASSWORD_KEY = "password";

    private final String BASIC_AUTH = "BASIC_AUTH";

    /**
     * Creates a subscriptionObject with REST/POST capabilities.
     * @param subscriptionName
     * @throws IOException
     */
    public RestPostSubscriptionObject(String subscriptionName) throws IOException {
        super(RestPostSubscriptionObject.class, subscriptionName);
        subscriptionJson.put(NOTIFICATION_TYPE_KEY, NOTIFICATION_TYPE);
    }

    /**
     * Sets the field authenticationType to BASIC_AUTH together with the username and password.
     * @param username
     * @param password
     */
    @Deprecated
    public void setBasicAuth(String username, String password) {
        setAuthenticationType(BASIC_AUTH);
        setUsername(username);
        setPassword(password);
    }

    /**
     * Sets the field authenticationType to given value
     * @param authenticationType
     * @return RestPostSubscriptionObject
     */
    public RestPostSubscriptionObject setAuthenticationType(String authenticationType) {
        subscriptionJson.put(AUTHENTICATION_TYPE_KEY, authenticationType);
        return this;
    }

    /**
     * Sets the field username in a subscription.
     * @param username
     * @return RestPostSubscriptionObject
     */
    public RestPostSubscriptionObject setUsername(String username) {
        subscriptionJson.put(USERNAME_KEY, username);
        return this;
    }

    /**
     * Sets the field password in a subscription.
     * @param password
     * @return RestPostSubscriptionObject
     */
    public RestPostSubscriptionObject setPassword(String password) {
        subscriptionJson.put(PASSWORD_KEY, password);
        return this;
    }
}
