package com.ericsson.eiffelcommons.subscriptionobject;

import java.io.IOException;

public class RestPostSubscriptionObject extends SubscriptionObject {

    private final String NOTIFICATION_TYPE_KEY = "notificationType";
    private final String NOTIFICATION_TYPE = "REST_POST";

    private final String AUTHENTICATION_TYPE_KEY = "authenticationType";
    private final String USERNAME_KEY = "userName";
    private final String PASSWORD_KEY = "password";

    /**
     * This sets up an enum for AuthenticationTypes
     *
     */
    public enum AuthenticationType {
        NO_AUTH, BASIC_AUTH, BASIC_AUTH_JENKINS_CSRF;
    }


    /**
     * Creates a subscriptionObject with REST/POST capabilities.
     * @param subscriptionName
     * @throws IOException
     */
    public RestPostSubscriptionObject(String subscriptionName) throws IOException {
        super(subscriptionName);

        subscriptionJson.put(NOTIFICATION_TYPE_KEY, NOTIFICATION_TYPE);
    }

    /**
     * Sets the field authenticationType to BASIC_AUTH together with the username and password.
     * @param username
     * @param password
     */
    @Deprecated
    public void setBasicAuth(String username, String password) {
        setAuthenticationType(AuthenticationType.BASIC_AUTH);
        setUsername(username);
        setPassword(password);
    }

    /**
     * Sets the field authenticationType to given value
     * @param basicAuth
     * @return RestPostSubscriptionObject
     */
    public RestPostSubscriptionObject setAuthenticationType(AuthenticationType basicAuth) {
        subscriptionJson.put(AUTHENTICATION_TYPE_KEY, basicAuth.name());
        return this;
    }

    /**
     * Sets the field username in a subscription.
     * @param username
     * @param password
     * @return RestPostSubscriptionObject
     */
    public RestPostSubscriptionObject setUsername(String username) {
        subscriptionJson.put(USERNAME_KEY, username);
        return this;
    }

    /**
     * Sets the field password in a subscription.
     * @param username
     * @param password
     * @return RestPostSubscriptionObject
     */
    public RestPostSubscriptionObject setPassword(String password) {
        subscriptionJson.put(PASSWORD_KEY, password);
        return this;
    }
}
