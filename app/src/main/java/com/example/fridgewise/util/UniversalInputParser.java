package com.example.fridgewise.util;

import com.example.fridgewise.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UniversalInputParser {

    public static class ParsedResult {
        public String section;
        public String[] fields;
        public Map<String, String> values;
        public boolean isValid;

        public ParsedResult(String section, String[] fields) {
            this.section = section;
            this.fields = fields;
            this.values = new HashMap<>();
            this.isValid = false;
        }

        public String get(String field, String defaultValue) {
            String val = values.get(field);
            return (val == null || val.isEmpty()) ? defaultValue : val;
        }
    }

    public static class SectionMetadata {
        public int iconRes;
        public int colorRes;
        public String title;
        public String[] allFields;

        public SectionMetadata(int iconRes, int colorRes, String title, String[] allFields) {
            this.iconRes = iconRes;
            this.colorRes = colorRes;
            this.title = title;
            this.allFields = allFields;
        }
    }

    private static final Map<String, String[]> TEMPLATES = new HashMap<>();
    private static final Map<String, String> HINTS = new HashMap<>();
    private static final Map<String, SectionMetadata> METADATA = new HashMap<>();

    static {
        // Task: name, priority, time, date, note
        String[] taskFields = {"name", "priority", "time", "date", "note"};
        TEMPLATES.put("task", taskFields);
        HINTS.put("task", "task_[name]_[priority]_[time]_[date]_[note]");
        METADATA.put("task", new SectionMetadata(
                R.drawable.ic_todo_item,
                R.color.orange_warning,
                "Task & Reminder",
                taskFields));

        // Shop: item, quantity, unit, price, note
        String[] shopFields = {"item", "quantity", "unit", "price", "note"};
        TEMPLATES.put("shop", shopFields);
        HINTS.put("shop", "shop_[item]_[quantity]_[unit]_[price]_[note]");
        METADATA.put("shop", new SectionMetadata(
                R.drawable.v02_img_icons_shopping,
                R.color.green_primary,
                "Shopping Item",
                shopFields));

        // Med: name, type, time, date, dosage
        String[] medFields = {"name", "type", "time", "date", "dosage"};
        TEMPLATES.put("med", medFields);
        HINTS.put("med", "med_[name]_[type]_[time]_[date]_[dosage]");
        METADATA.put("med", new SectionMetadata(
                R.drawable.med_image_07,
                R.color.purple_primary,
                "Medicine Reminder",
                medFields));

        // Inv (Food): item, category, quantity, unit, expiry
        String[] invFields = {"item", "category", "quantity", "unit", "expiry"};
        TEMPLATES.put("inv", invFields);
        HINTS.put("inv", "inv_[item]_[category]_[quantity]_[unit]_[expiry]");
        METADATA.put("inv", new SectionMetadata(
                R.drawable.add_img01,
                R.color.green_primary,
                "Inventory Update",
                invFields));

        // Cust: name
        String[] custFields = {"name"};
        TEMPLATES.put("cust", custFields);
        HINTS.put("cust", "cust_[name]");
        METADATA.put("cust", new SectionMetadata(
                R.drawable.v02_img_icons_doccc,
                R.color.doc_primary,
                "Custom Entry",
                custFields));
    }

    public static String getHint(String section) {
        return HINTS.getOrDefault(section.toLowerCase(), "Use section_value_value...");
    }

    public static SectionMetadata getMetadata(String section) {
        return METADATA.get(section.toLowerCase());
    }

    public static String[] getSections() {
        return new String[]{"task", "shop", "med", "inv", "cust"};
    }

    public static boolean isSection(String prefix) {
        return TEMPLATES.containsKey(prefix.toLowerCase());
    }

    public static ParsedResult parse(String input) {
        if (input == null || !input.contains("_")) {
            return null;
        }

        String[] parts = input.split("_", -1);
        String section = parts[0].toLowerCase();

        if (!TEMPLATES.containsKey(section)) {
            return null;
        }

        String[] fields = TEMPLATES.get(section);
        ParsedResult result = new ParsedResult(section, fields);

        for (int i = 0; i < fields.length; i++) {
            if (i + 1 < parts.length) {
                String val = parts[i + 1].trim();
                if (!val.isEmpty()) {
                    result.values.put(fields[i], val);
                }
            }
        }

        // Mandatory check: The first field (name/item) must be present
        String name = result.values.get(fields[0]);
        if (name != null && !name.isEmpty()) {
            result.isValid = true;
            resolveDefaults(result);
        }

        return result;
    }

    private static void resolveDefaults(ParsedResult result) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // Handle Date Logic
        if (result.values.containsKey("date") || result.values.containsKey("expiry")) {
            String field = result.values.containsKey("date") ? "date" : "expiry";
            String dateVal = result.values.get(field);
            
            if (dateVal != null) {
                dateVal = dateVal.toLowerCase();
                if (dateVal.equals("today")) {
                    result.values.put(field, sdfDate.format(cal.getTime()));
                } else if (dateVal.equals("tomorrow")) {
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    result.values.put(field, sdfDate.format(cal.getTime()));
                }
            }
        } else if (result.section.equals("task") || result.section.equals("med")) {
            // Default task/med date is Today if not specified
            result.values.put("date", sdfDate.format(cal.getTime()));
        }

        // Handle Time Logic
        if (result.section.equals("task") || result.section.equals("med")) {
            String time = result.values.get("time");
            if (time == null || time.isEmpty()) {
                // Default task time if not specified
                result.values.put("time", "9:00 AM");
            } else {
                // Clean up time format if needed (e.g. "7pm" -> "7:00 PM")
                result.values.put("time", formatTime(time));
            }
        }

        // Handle Priority
        if (result.section.equals("task")) {
            String prio = result.values.get("priority");
            if (prio == null || prio.isEmpty()) {
                result.values.put("priority", "Medium");
            }
        }
    }

    private static String formatTime(String time) {
        time = time.toLowerCase().replace(" ", "");
        if (time.matches("\\d{1,2}(am|pm)")) {
            String period = time.substring(time.length() - 2).toUpperCase();
            String hour = time.substring(0, time.length() - 2);
            return hour + ":00 " + period;
        }
        return time.toUpperCase(); // Fallback to raw caps
    }
}
