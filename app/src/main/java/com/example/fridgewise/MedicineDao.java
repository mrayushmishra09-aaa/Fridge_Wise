package com.example.fridgewise;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;
@Dao
public interface MedicineDao {
    @Insert
    long insert (MedicineEntity medicine);
    @Delete
    void delete (MedicineEntity medicine);
    @Update
    void update (MedicineEntity medicine);
    @Query("SELECT * FROM medicine_table")
    List<MedicineEntity> getAllMedicines();

}
