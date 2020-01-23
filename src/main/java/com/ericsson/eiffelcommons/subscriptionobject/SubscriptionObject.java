package com.ericsson.eiffelcommons.subscriptionobject;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ericsson.eiffelcommons.utils.FileUtils;

import lombok.Getter;

public abstract class SubscriptionObject<T extends SubscriptionObject<?>> {
    private static final String SUBSCRIPTION_TEMPLATE_PATH = "subscriptionsTemplate.json";

    // reference to self as the subclass type
    protected final T self;

    @Getter
    protected JSONObject subscriptionJson;

    /**
     * Creates a subscription with a specific name.
     *
     * @param subscriptionName
     * @throws IOException
     */
    public SubscriptionObject(final Class<T> selfClass, String subscriptionName) throws IOException {
        subscriptionJson = FileUtils.getResourceFileAsJsonObject(SUBSCRIPTION_TEMPLATE_PATH);
        subscriptionJson.put("subscriptionName", subscriptionName);
        this.self = selfClass.cast(this);
    }

    /**
     * Adds a notification body to the subscriptionObject.
     *
     * @param notificationBody
     * @return SubscriptionObject<T>
     */
    public T addNotificationBody(final String notificationBody) {
        return addNotificationMessageKeyValue("", notificationBody);
    }

    /**
     * Adds a notification message to the subscriptionObject as key value.
     *
     * @param notificationKey
     * @param notificationValue
     * @return SubscriptionObject<T>
     */
    public T addNotificationMessageKeyValue(String notificationKey, String notificationValue) {
        JSONObject keyValue = new JSONObject();
        keyValue.put("formkey", notificationKey);
        keyValue.put("formvalue", notificationValue);

        JSONArray notificationMessageKeyValue = subscriptionJson.getJSONArray(
                "notificationMessageKeyValues");
        notificationMessageKeyValue.put(keyValue);
        return this.self;
    }

    /**
     * Adds a condition to a requirement, the condition is objectNode containing key value for the
     * jmesPath expression
     *
     * @param requirementIndex
     * @param condition
     * @return SubscriptionObject<T>
     */
    public T addConditionToRequirement(int requirementIndex, JSONObject condition) {
        JSONArray requirements = subscriptionJson.getJSONArray("requirements");
        JSONObject requirement = requirements.getJSONObject(requirementIndex);
        JSONArray conditions = requirement.getJSONArray("conditions");
        conditions.put(condition);
        return this.self;
    }

    /**
     * Sets the field notificationMeta to the wanted value
     *
     * @param notificationMeta
     * @return SubscriptionObject<T>
     */
    public T setNotificationMeta(String notificationMeta) {
        subscriptionJson.put("notificationMeta", notificationMeta);
        return this.self;
    }

    /**
     * Set the restPostBodyMediaType field in the subscriptions to wanted value for example
     * "application/x-www-form-urlencoded"
     *
     * @param restPostBodyMediaType
     * @return SubscriptionObject<T>
     */
    public T setRestPostBodyMediaType(String restPostBodyMediaType) {
        subscriptionJson.put("restPostBodyMediaType", restPostBodyMediaType);
        return this.self;
    }

    /**
     * Returns the subscription as an array with the subscription. (This is the way
     * Eiffel-intelligence stores it.)
     *
     * @return ArrayNode
     */
    public JSONArray getAsSubscriptions() {
        JSONArray subscriptions = new JSONArray();
        subscriptions.put(subscriptionJson);

        return subscriptions;
    }

    @Override
    public String toString() {
        return subscriptionJson.toString();
    }
}
