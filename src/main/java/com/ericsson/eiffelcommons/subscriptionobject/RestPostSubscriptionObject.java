/*
   Copyright 2019 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
