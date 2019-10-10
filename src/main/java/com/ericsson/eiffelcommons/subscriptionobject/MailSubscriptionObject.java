package com.ericsson.eiffelcommons.subscriptionobject;

import java.io.IOException;

public class MailSubscriptionObject extends SubscriptionObject<MailSubscriptionObject> {

    private final String EMAIL_SUBJECT = "emailSubject";

    /**
     * Creates a subscriptionObject with MAIL capabilities.
     * @param subscriptionName
     * @throws IOException
     */
    public MailSubscriptionObject(String subscriptionName) throws IOException {
        super(MailSubscriptionObject.class, subscriptionName);

        subscriptionJson.put("notificationType", "MAIL");
    }

    /**
     * Sets the field emailSubject to given value
     * @param emailSubject
     * @return MailSubscriptionObject
     */
    public MailSubscriptionObject setEmailSubject(final String emailSubject) {
        subscriptionJson.put(EMAIL_SUBJECT, emailSubject);
        return this;
    }
}
