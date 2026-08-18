package com.example.fridgewise;

import java.util.Date;

/**
 * Unified model for Expiry Alerts (Food and Medicine).
 */
public class AlertItem {
    public enum Type { FOOD, MEDICINE }

    private int id;
    private String name;
    private String category;
    private String date; // Expiry or Start date
    private Date dateObject;
    private Type type;
    private String status; // "Expired", "Today", "Soon"

    public AlertItem(int id, String name, String category, String date, Date dateObject, Type type, String status) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.date = date;
        this.dateObject = dateObject;
        this.type = type;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public Date getDateObject() { return dateObject; }
    public Type getType() { return type; }
    public String getStatus() { return status; }
}