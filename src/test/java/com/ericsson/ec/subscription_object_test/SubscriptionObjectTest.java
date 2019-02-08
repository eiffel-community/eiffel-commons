package com.ericsson.ec.subscription_object_test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.ec.helpers.MediaType;
import com.ericsson.ec.subscriptionobject.RestPostSubscriptionObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SubscriptionObjectTest {

    RestPostSubscriptionObject restPostSubscription;

    @Before
    public void setup() throws IOException {
        restPostSubscription = new RestPostSubscriptionObject("mySubscription");
    }

    @Test
    public void addConditionToRequirement() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode condition = mapper.createObjectNode();
        condition.put("test", "'test'");
        restPostSubscription.addConditionToRequirement(0, condition);

        ArrayNode actualRequirement = (ArrayNode) restPostSubscription.getSubscriptionJson().get("requirements");
        ObjectNode actualCondition = (ObjectNode) actualRequirement.get(0);

        assertEquals("{\"conditions\":[{\"test\":\"'test'\"}]}", actualCondition.toString());
    }

    @Test
    public void testAddNotificationMessageKeyValue() {
        restPostSubscription.addNotificationMessageKeyValue("jmespath", "'test'");

        ArrayNode actualNotificationMessageKeyValue = (ArrayNode) restPostSubscription.getSubscriptionJson().get("notificationMessageKeyValues");
        assertEquals("[{\"formkey\":\"jmespath\",\"formvalue\":\"'test'\"}]", actualNotificationMessageKeyValue.toString());
    }

    @Test
    public void testSetNotificationMeta() {
        restPostSubscription.setNotificationMeta("http://localhost:8080");

        String actualNotificationMeta = restPostSubscription.getSubscriptionJson().get("notificationMeta").asText();
        assertEquals("http://localhost:8080", actualNotificationMeta);
    }

    @Test
    public void testSetBasicAuth() {
        restPostSubscription.setBasicAuth("admin", "admin");

        String actualUsername = restPostSubscription.getSubscriptionJson().get("userName").asText();
        String actualPassword = restPostSubscription.getSubscriptionJson().get("password").asText();
        String actualAuthenticationType = restPostSubscription.getSubscriptionJson().get("authenticationType").asText();

        assertEquals("admin", actualUsername);
        assertEquals("admin", actualPassword);
        assertEquals("BASIC_AUTH", actualAuthenticationType);
    }

    @Test
    public void testSetRestPostBodyMediaType() {
        restPostSubscription.setRestPostBodyMediaType(MediaType.APPLICATION_FORM_URLENCODED);

        String actualRestPostBodyMediaType = restPostSubscription.getSubscriptionJson().get("restPostBodyMediaType").asText();
        assertEquals("application/x-www-form-urlencoded", actualRestPostBodyMediaType);
    }
}
