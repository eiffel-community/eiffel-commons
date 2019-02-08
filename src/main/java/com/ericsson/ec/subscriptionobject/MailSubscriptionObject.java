package com.ericsson.ec.subscriptionobject;

import java.io.IOException;

public class MailSubscriptionObject extends SubscriptionObject {

    public MailSubscriptionObject(String subscriptionName) throws IOException {
        super(subscriptionName);

        subscriptionJson.put("notificationType", "MAIL");
    }
}
