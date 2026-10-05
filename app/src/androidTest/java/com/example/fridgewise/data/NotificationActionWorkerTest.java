package com.example.fridgewise.data;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.testing.TestWorkerBuilder;

import com.example.fridgewise.model.MedicineEntity;
import com.example.fridgewise.util.NotificationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(AndroidJUnit4.class)
public class NotificationActionWorkerTest {
    private Context context;
    private Executor executor;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        executor = Executors.newSingleThreadExecutor();
    }

    @Test
    public void testTakeDoseAction() {
        AppDatabase db = AppDatabase.getInstance(context);
        
        // Setup fresh data
        MedicineEntity med = new MedicineEntity();
        med.setMedicineName("Work Test Med");
        med.setDosage("1");
        med.setQuantity("10");
        long id = db.medicineDao().insert(med);

        Data input = new Data.Builder()
                .putString("action", NotificationHelper.ACTION_TAKE_DOSE)
                .putInt("item_id_actual", (int) id)
                .putInt("id", 100)
                .build();

        NotificationActionWorker worker = TestWorkerBuilder.from(context, NotificationActionWorker.class, executor)
                .setInputData(input)
                .build();

        ListenableWorker.Result result = worker.doWork();

        assertEquals(ListenableWorker.Result.success(), result);

        // Verify DB update
        MedicineEntity updated = db.medicineDao().getMedicineById((int) id);
        assertEquals("9", updated.getQuantity());
        
        // Cleanup
        db.medicineDao().delete(updated);
    }
}
