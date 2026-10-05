package com.example.fridgewise.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fridgewise.model.MedicineEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MedicineDaoTest {
    private AppDatabase db;
    private MedicineDao medicineDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        medicineDao = db.medicineDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testInsertAndGetMedicine() {
        MedicineEntity medicine = new MedicineEntity();
        medicine.setMedicineName("Test Med");
        medicine.setDosage("1");
        medicine.setQuantity("10");
        
        long id = medicineDao.insert(medicine);
        
        MedicineEntity retrieved = medicineDao.getMedicineById((int) id);
        assertNotNull(retrieved);
        assertEquals("Test Med", retrieved.getMedicineName());
    }

    @Test
    public void testGetAllMedicines() {
        MedicineEntity m1 = new MedicineEntity();
        m1.setMedicineName("Med 1");
        MedicineEntity m2 = new MedicineEntity();
        m2.setMedicineName("Med 2");
        
        medicineDao.insert(m1);
        medicineDao.insert(m2);
        
        List<MedicineEntity> all = medicineDao.getAllMedicines();
        assertEquals(2, all.size());
    }
}
