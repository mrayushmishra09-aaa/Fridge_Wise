package com.example.fridgewise.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.AlarmManager;
import android.content.Context;

import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.data.NotificationInteractionDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

@RunWith(RobolectricTestRunner.class)
public class ReminderCoordinatorTest {

    @Mock
    private Context mockContext;
    @Mock
    private AlarmManager mockAlarmManager;
    @Mock
    private AppDatabase mockDb;
    @Mock
    private NotificationInteractionDao mockDao;

    private ReminderCoordinator coordinator;
    private MockedStatic<AppDatabase> mockedDbStatic;
    private MockedStatic<NotificationPersonalityEngine> mockedPersonalityStatic;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockAlarmManager);
        
        mockedDbStatic = mockStatic(AppDatabase.class);
        mockedDbStatic.when(() -> AppDatabase.getInstance(any())).thenReturn(mockDb);
        when(mockDb.notificationInteractionDao()).thenReturn(mockDao);

        // Mock interaction history
        when(mockDao.getNotificationCountSince(anyLong())).thenReturn(0);
        when(mockDao.getDismissalsForItem(any(), anyInt())).thenReturn(new ArrayList<>());

        mockedPersonalityStatic = mockStatic(NotificationPersonalityEngine.class);
        mockedPersonalityStatic.when(() -> NotificationPersonalityEngine.getHumanizedMessage(any(), anyBoolean(), anyInt()))
                .thenReturn("Humanized Title");

        // Use a synchronous executor for testing to ensure thread-local static mocks work
        ExecutorService syncExecutor = mock(ExecutorService.class);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(syncExecutor).execute(any(Runnable.class));

        coordinator = new ReminderCoordinator(mockContext, syncExecutor);
    }

    @After
    public void tearDown() {
        mockedDbStatic.close();
        mockedPersonalityStatic.close();
    }

    @Test
    public void testScheduleAlarm() {
        long time = System.currentTimeMillis() + 5000;
        coordinator.schedule("MEDICINE", 1, "Title", "Message", time, 0, "group");
        
        verify(mockAlarmManager).setExactAndAllowWhileIdle(eq(AlarmManager.RTC_WAKEUP), anyLong(), any());
    }
}
