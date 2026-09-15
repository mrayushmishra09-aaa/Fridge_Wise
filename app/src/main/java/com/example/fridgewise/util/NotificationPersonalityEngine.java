package com.example.fridgewise.util;

import java.util.Calendar;
import java.util.Random;

public class NotificationPersonalityEngine {

    private static final String[] MORNING_GREETINGS = {
            "Good morning! ",
            "Hope you slept well. ",
            "Rise and shine! "
    };

    private static final String[] AFTERNOON_GREETINGS = {
            "Quick check-in! ",
            "Hope your day is going great. ",
            "Hello! "
    };

    private static final String[] EVENING_GREETINGS = {
            "Before the day winds down, ",
            "Good evening. ",
            "Wrapping up for today? "
    };

    private static final String[] FOLLOW_UP_GENTLE = {
            "Just a little reminder — this one is still waiting for you.",
            "Hey, this one is still on your list.",
            "Don't forget about this little thing."
    };

    private static final String[] FOLLOW_UP_RESPECTFUL = {
            "No pressure, but here it is whenever you're ready.",
            "Just keeping this here for you.",
            "Whenever you have a moment, this is waiting."
    };

    public static String getHumanizedMessage(String baseTask, boolean isFollowUp, int dismissalsCount) {
        StringBuilder sb = new StringBuilder();
        
        // Add time-based greeting only for new notifications
        if (!isFollowUp) {
            sb.append(getGreetingByTime());
        }

        if (isFollowUp) {
            if (dismissalsCount > 2) {
                sb.append(getRandom(FOLLOW_UP_RESPECTFUL));
            } else {
                sb.append(getRandom(FOLLOW_UP_GENTLE));
            }
        } else {
            sb.append("You have a ").append(baseTask).append(" to take care of.");
        }

        return sb.toString();
    }

    private static String getGreetingByTime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return getRandom(MORNING_GREETINGS);
        if (hour >= 12 && hour < 17) return getRandom(AFTERNOON_GREETINGS);
        return getRandom(EVENING_GREETINGS);
    }

    private static String getRandom(String[] array) {
        return array[new Random().nextInt(array.length)];
    }
}
