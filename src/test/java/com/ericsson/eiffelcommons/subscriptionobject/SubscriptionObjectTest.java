package com.ericsson.eiffelcommons.subscriptionobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eiffelcommons.constants.MediaType;
import com.ericsson.eiffelcommons.subscriptionobject.MailSubscriptionObject;
import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;

public class SubscriptionObjectTest {

    RestPostSubscriptionObject restPostSubscription;
    MailSubscriptionObject mailSubscription;

    @Before
    public void setup() throws IOException {
        restPostSubscription = new RestPostSubscriptionObject("mySubscription");
        mailSubscription = new MailSubscriptionObject("myMailSubscription");
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
                                                                          .getJSONArray(
                                                                                  "notificationMessageKeyValues");
        assertEquals("[{\"formkey\":\"jmespath\",\"formvalue\":\"'test'\"}]",
                actualNotificationMessageKeyValue.toString());
    }

    @Test
    public void testSetNotificationMeta() {
        restPostSubscription.setNotificationMeta("http://localhost:8080");

        String actualNotificationMeta = restPostSubscription.getSubscriptionJson()
                                                            .getString("notificationMeta");
        assertEquals("http://localhost:8080", actualNotificationMeta);
    }

    @Test
    public void testSetBasicAuth() {
        restPostSubscription.setBasicAuth("admin", "admin");

        String actualUsername = restPostSubscription.getSubscriptionJson()
                                                    .getString("userName");
        String actualPassword = restPostSubscription.getSubscriptionJson()
                                                    .getString("password");
        String actualAuthenticationType = restPostSubscription.getSubscriptionJson()
                                                              .getString("authenticationType");

        assertEquals("admin", actualUsername);
        assertEquals("admin", actualPassword);
        assertEquals("BASIC_AUTH", actualAuthenticationType);
    }

    @Test
    public void testSetRestPostBodyMediaType() {
        restPostSubscription.setRestPostBodyMediaType(MediaType.APPLICATION_FORM_URLENCODED);

        String actualRestPostBodyMediaType = restPostSubscription.getSubscriptionJson()
                                                                 .getString("restPostBodyMediaType");
        assertEquals("application/x-www-form-urlencoded", actualRestPostBodyMediaType);
    }

    @Test
    public void testSetNotificationBody() {
        String body = "Body";
        mailSubscription.addNotificationBody(body);

        String actualNotificationBody = mailSubscription.getSubscriptionJson()
                                                        .get("notificationMessageKeyValues")
                                                        .toString();
        assertTrue(actualNotificationBody.contains(body));
    }

    @Test
    public void testSetEmailSubject() {
        String subject = "MySubject";
        mailSubscription.setEmailSubject(subject);

        String actualEmailSubject = mailSubscription.getSubscriptionJson()
                                                    .getString("emailSubject");
        assertEquals(subject, actualEmailSubject);
    }

    @Test
    public void testChaining() {
        restPostSubscription.addNotificationMessageKeyValue("Key", "value")
                            .setAuthenticationType("test_type")
                            .setPassword("my_password")
                            .setUsername("username")
                            .addNotificationMessageKeyValue("key2", "value2")
                            .setNotificationMeta("SomeURL");
        JSONObject subscription = restPostSubscription.getSubscriptionJson();

        assertEquals("my_password", subscription.getString("password"));
    }
}
