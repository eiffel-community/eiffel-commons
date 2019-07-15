package com.ericsson.eiffelcommons.subscriptionobject;

import java.io.IOException;

public class MailSubscriptionObject extends SubscriptionObject<MailSubscriptionObject> {

    /**
     * Creates a subscriptionObject with MAIL capabilities.
     * @param subscriptionName
     * @throws IOException
     */
    public MailSubscriptionObject(String subscriptionName) throws IOException {
        super(MailSubscriptionObject.class, subscriptionName);

        subscriptionJson.put("notificationType", "MAIL");
    }
}
