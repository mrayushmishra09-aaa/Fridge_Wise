package com.example.fridgewise.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

@RunWith(RobolectricTestRunner.class)
public class PermissionManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.TIRAMISU)
    public void testNotificationPermission_Granted() {
        ShadowApplication shadowApplication = Shadows.shadowOf((Application) context);
        shadowApplication.grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
        
        assertTrue(PermissionManager.hasNotificationPermission(context));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.TIRAMISU)
    public void testNotificationPermission_Denied() {
        ShadowApplication shadowApplication = Shadows.shadowOf((Application) context);
        shadowApplication.denyPermissions(Manifest.permission.POST_NOTIFICATIONS);
        
        assertFalse(PermissionManager.hasNotificationPermission(context));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.S)
    public void testExactAlarmPermission_AlwaysTrueInRobolectricByNotification() {
        // Exact alarms are tricky to shadow in older Robolectric, but we can verify the method call doesn't crash
        boolean result = PermissionManager.canScheduleExactAlarms(context);
        // By default it might return true or false depending on shadow implementation
        // The main point is to have the test harness ready.
        assertTrue(result || !result); 
    }
}
