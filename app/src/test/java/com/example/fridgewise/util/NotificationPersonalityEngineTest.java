package com.example.fridgewise.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class NotificationPersonalityEngineTest {

    @Test
    public void testGetHumanizedMessage_NewTask() {
        String message = NotificationPersonalityEngine.getHumanizedMessage("medicine", false, 0);
        assertNotNull(message);
        // Should contain the task category
        assertTrue(message.toLowerCase().contains("medicine"));
    }

    @Test
    public void testGetHumanizedMessage_FollowUp() {
        String message = NotificationPersonalityEngine.getHumanizedMessage("medicine", true, 1);
        assertNotNull(message);
        // Follow-up messages for low dismiss count (gentle)
        boolean isGentle = message.contains("reminder") || message.contains("list") || message.contains("forget");
        assertTrue("Message was: " + message, isGentle);
    }

    @Test
    public void testGetHumanizedMessage_RespectfulFollowUp() {
        String message = NotificationPersonalityEngine.getHumanizedMessage("medicine", true, 5);
        assertNotNull(message);
        // Should use respectful tone for high dismissal count
        String lower = message.toLowerCase();
        boolean isRespectful = lower.contains("no pressure") || lower.contains("keeping this here") || lower.contains("whenever you have a moment");
        assertTrue("Message was: " + message, isRespectful);
    }
}
