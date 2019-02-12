package com.ericsson.eiffelcommons.subscriptionobject;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ericsson.eiffelcommons.utils.Utils;

import lombok.Getter;

public abstract class SubscriptionObject {
    private static final String SUBSCRIPTION_TEMPLATE_PATH = "subscriptionsTemplate.json";

    @Getter
    protected JSONObject subscriptionJson;

    /**
     * Creates a subscription with a specific name.
     * @param subscriptionName
     * @throws IOException
     */
    public SubscriptionObject(String subscriptionName) throws IOException {
        subscriptionJson = Utils.getResourceFileAsJsonObject(SUBSCRIPTION_TEMPLATE_PATH);
        subscriptionJson.put("subscriptionName", subscriptionName);
    }

    /**
     * Adds a notification message to the subscriptionObject as key value.
     * @param notificationKey
     * @param notificationValue
     */
    public void addNotificationMessageKeyValue(String notificationKey, String notificationValue) {
        JSONObject keyValue = new JSONObject();
        keyValue.put("formkey", notificationKey);
        keyValue.put("formvalue", notificationValue);

        JSONArray notificationMessageKeyValue = subscriptionJson.getJSONArray("notificationMessageKeyValues");
        notificationMessageKeyValue.put(keyValue);
    }

    /**
     * Adds a condition to a requirement, the condition is objectNode containing key value for the jmesPath expression
     * @param requirementIndex
     * @param condition
     */
    public void addConditionToRequirement(int requirementIndex, JSONObject condition) {
        JSONArray requirements =  subscriptionJson.getJSONArray("requirements");
        JSONObject requirement = requirements.getJSONObject(requirementIndex);
        JSONArray conditions = requirement.getJSONArray("conditions");
        conditions.put(condition);
    }

    /**
     * Sets the field notificationMeta to the wanted value
     * @param notificationMeta
     */
    public void setNotificationMeta(String notificationMeta) {
        subscriptionJson.put("notificationMeta", notificationMeta);
    }

    /**
     * Set the restPostBodyMediaType field in the subscriptions to wanted value for example "application/x-www-form-urlencoded"
     * @param value
     */
    public void setRestPostBodyMediaType(String restPostBodyMediaType) {
        subscriptionJson.put("restPostBodyMediaType", restPostBodyMediaType);
    }

    /**
     * Returns the subscription as an array with the subscription. (This is the way Eiffel-intelligence stores it.)
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
