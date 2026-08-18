package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertsFragment extends Fragment {

    private RecyclerView rvAlerts;
    private AlertAdapter adapter;
    private ChipGroup chipGroup;
    private LinearLayout emptyState;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
    private List<AlertItem> allAlerts = new ArrayList<>();

    public AlertsFragment() {
        // Required empty public constructor
    }

    public static AlertsFragment newInstance() {
        return new AlertsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAlerts = view.findViewById(R.id.rvAlerts);
        chipGroup = view.findViewById(R.id.chipGroupFilters);
        emptyState = view.findViewById(R.id.emptyStateLayout);

        rvAlerts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AlertAdapter();
        rvAlerts.setAdapter(adapter);

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> filterAlerts());

        loadAlerts();
    }

    private void loadAlerts() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<AlertItem> tempList = new ArrayList<>();

            Calendar cal = Calendar.getInstance();
            Date today = resetTime(cal.getTime());
            
            cal.add(Calendar.DAY_OF_YEAR, 7);
            Date nextWeek = resetTime(cal.getTime());

            // 1. Load Food Items
            List<FoodItem> foods = db.foodItemDao().getAllItems();
            for (FoodItem food : foods) {
                try {
                    Date expiry = dateFormat.parse(food.getExpiryDate());
                    if (expiry != null) {
                        expiry = resetTime(expiry);
                        String status = getStatus(expiry, today, nextWeek);
                        if (status != null) {
                            tempList.add(new AlertItem(food.getId(), food.getName(), food.getCategory(), 
                                    food.getExpiryDate(), expiry, AlertItem.Type.FOOD, status));
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // 2. Load Medicine Items
            List<MedicineEntity> medicines = db.medicineDao().getAllMedicines();
            for (MedicineEntity med : medicines) {
                try {
                    // Medicines might have a start date or expiry (currently we use startDate as a proxy for reminder context)
                    Date start = dateFormat.parse(med.getStartDate());
                    if (start != null) {
                        start = resetTime(start);
                        String status = getStatus(start, today, nextWeek);
                        if (status != null) {
                            tempList.add(new AlertItem(med.getId(), med.getMedicineName(), med.getMedicineType(), 
                                    med.getStartDate(), start, AlertItem.Type.MEDICINE, status));
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            allAlerts = tempList;

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> filterAlerts());
            }
        }).start();
    }

    private String getStatus(Date targetDate, Date today, Date nextWeek) {
        if (targetDate.before(today)) return "Expired";
        if (targetDate.equals(today)) return "Today";
        if (targetDate.after(today) && targetDate.before(nextWeek)) return "Soon";
        return null; // Far in future
    }

    private void filterAlerts() {
        int checkedId = chipGroup.getCheckedChipId();
        List<AlertItem> filteredList = new ArrayList<>();

        for (AlertItem item : allAlerts) {
            if (checkedId == R.id.chipAll) {
                filteredList.add(item);
            } else if (checkedId == R.id.chipToday && item.getStatus().equals("Today")) {
                filteredList.add(item);
            } else if (checkedId == R.id.chipExpired && item.getStatus().equals("Expired")) {
                filteredList.add(item);
            } else if (checkedId == R.id.chipThisWeek && (item.getStatus().equals("Soon") || item.getStatus().equals("Today"))) {
                filteredList.add(item);
            }
        }

        adapter.setAlerts(filteredList);
        emptyState.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private Date resetTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}