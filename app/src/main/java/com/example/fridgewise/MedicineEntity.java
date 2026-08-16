package com.example.fridgewise;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity (tableName = "medicine_table")
public class MedicineEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String medicineName;
    private String medicineType;
    private String quantity;
    private String unit;
    private String dosage;
    private String frequency;
    private String startDate;
    private String startTime;
    private boolean reminderOn;
    private int iconResId;
    private String lastTakenDate;

    public MedicineEntity() {}

    // getters and setters


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getMedicineName(){
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }
    public String getMedicineType(){
        return medicineType;
    }

    public void setMedicineType(String medicineType) {
        this.medicineType = medicineType;
    }
    public String getQuantity(){
        return quantity;
    }
    public  void setQuantity(String quantity){
        this.quantity = quantity;
    }
    public String getUnit(){
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
    public String getDosage(){
        return dosage;
    }
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
    public String getFrequency(){
        return frequency;
    }
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    public String getStartDate(){
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getStartTime(){
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public boolean isReminderOn() {
        return reminderOn;
    }
    public void setReminderOn(boolean reminderOn) {
        this.reminderOn = reminderOn;
    }
    public int getIconResId() {
        return iconResId;
    }
    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getLastTakenDate() {
        return lastTakenDate;
    }

    public void setLastTakenDate(String lastTakenDate) {
        this.lastTakenDate = lastTakenDate;
    }

}
