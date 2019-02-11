package com.ericsson.eiffelcommons.subscriptionobjecttest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffelcommons.helpers.MediaType;
import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;

public class SubscriptionObjectTest {

    RestPostSubscriptionObject restPostSubscription;

    @Before
    public void setup() throws IOException {
        restPostSubscription = new RestPostSubscriptionObject("mySubscription");
    }

    @Test
    public void addConditionToRequirement() {
        JSONObject condition = new JSONObject();
        condition.put("test", "'test'");
        restPostSubscription.addConditionToRequirement(0, condition);

        JSONArray actualRequirement = restPostSubscription.getSubscriptionJson()
                .getJSONArray("requirements");
        JSONObject actualCondition = actualRequirement.getJSONObject(0);

        assertEquals("{\"conditions\":[{\"test\":\"'test'\"}]}", actualCondition.toString());
    }

    @Test
    public void testAddNotificationMessageKeyValue() {
        restPostSubscription.addNotificationMessageKeyValue("jmespath", "'test'");

        JSONArray actualNotificationMessageKeyValue = restPostSubscription.getSubscriptionJson()
                .getJSONArray("notificationMessageKeyValues");
        assertEquals("[{\"formkey\":\"jmespath\",\"formvalue\":\"'test'\"}]", actualNotificationMessageKeyValue.toString());
    }

    @Test
    public void testSetNotificationMeta() {
        restPostSubscription.setNotificationMeta("http://localhost:8080");

        String actualNotificationMeta = restPostSubscription.getSubscriptionJson()
                .get("notificationMeta")
                .toString();
        assertEquals("http://localhost:8080", actualNotificationMeta);
    }

    @Test
    public void testSetBasicAuth() {
        restPostSubscription.setBasicAuth("admin", "admin");

        String actualUsername = restPostSubscription.getSubscriptionJson()
                .get("userName")
                .toString();
        String actualPassword = restPostSubscription.getSubscriptionJson()
                .get("password")
                .toString();
        String actualAuthenticationType = restPostSubscription.getSubscriptionJson()
                .get("authenticationType")
                .toString();

        assertEquals("admin", actualUsername);
        assertEquals("admin", actualPassword);
        assertEquals("BASIC_AUTH", actualAuthenticationType);
    }

    @Test
    public void testSetRestPostBodyMediaType() {
        restPostSubscription.setRestPostBodyMediaType(MediaType.APPLICATION_FORM_URLENCODED);

        String actualRestPostBodyMediaType = restPostSubscription.getSubscriptionJson()
                .get("restPostBodyMediaType")
                .toString();
        assertEquals("application/x-www-form-urlencoded", actualRestPostBodyMediaType);
    }
}
