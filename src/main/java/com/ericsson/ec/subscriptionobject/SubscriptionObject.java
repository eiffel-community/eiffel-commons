package com.ericsson.ec.subscriptionobject;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;

abstract class SubscriptionObject {
    private static final String SUBSCRIPTION_TEMPLATE_PATH = "src/main/resources/subscriptionsTemplate.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    protected ObjectNode subscriptionJson;

    /**
     * Creates a subscription with a specific name.
     * @param subscriptionName
     * @throws IOException
     */
    public SubscriptionObject(String subscriptionName) throws IOException {
        URL subscriptionsInput = new File(SUBSCRIPTION_TEMPLATE_PATH).toURI().toURL();
        subscriptionJson = (ObjectNode) objectMapper.readTree(subscriptionsInput);
        subscriptionJson.put("subscriptionName", subscriptionName);
    }

    /**
     * Adds a notification message to the subscriptionObject as key value.
     * @param notificationKey
     * @param notificationValue
     */
    public void addNotificationMessageKeyValue(String notificationKey, String notificationValue) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode keyValue = objectMapper.createObjectNode();
        keyValue.put("formkey", notificationKey);
        keyValue.put("formvalue", notificationValue);

        ArrayNode notificationMessageKeyValue = ((ArrayNode) subscriptionJson.get("notificationMessageKeyValues"));
        notificationMessageKeyValue.add(keyValue);
    }

    /**
     * Adds a condition to a requirement, the condition is objectNode containing key value for the jmesPath expression
     * @param requirementIndex
     * @param condition
     */
    public void addConditionToRequirement(int requirementIndex, ObjectNode condition) {
        ArrayNode requirement = ((ArrayNode) subscriptionJson.get("requirements").get(requirementIndex).get("conditions"));
        requirement.add(condition);
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
    public ArrayNode getAsSubscriptions() {
        ArrayNode subscriptions = objectMapper.createArrayNode();
        subscriptions.add(subscriptionJson);

        return subscriptions;
    }

    @Override
    public String toString() {
        return subscriptionJson.toString();
    }
}
