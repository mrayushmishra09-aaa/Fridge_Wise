package com.example.fridgewise;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MedicineAddFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedicineAddFragment extends Fragment {

    public static final String ARG_MEDICINE = "medicine_item";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText medicineNameInput, quantityInput;
    private AutoCompleteTextView typeSpinner, unitSpinner, dosageSpinner, frequencySpinner;
    private TextView startDateText, timeText;
    private SwitchCompat reminderSwitch;
    private int selectedIconResId = R.drawable.medsec_image_09;
    private MedicineEntity existingMedicine;

    public MedicineAddFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MedicineAddFragment.
     */
    public static MedicineAddFragment newInstance(String param1, String param2) {
        MedicineAddFragment fragment = new MedicineAddFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_medicine_add, container, false);

        if (getArguments() != null) {
            existingMedicine = (MedicineEntity) getArguments().getSerializable(ARG_MEDICINE);
        }

        ImageView backbtn = view.findViewById(R.id.backBtn);
        backbtn.setOnClickListener(v->{
            getParentFragmentManager().popBackStack();
        });

        ImageView btnInfo = view.findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(v -> {
            AboutAddMedicineBottomSheet bottomSheet = new AboutAddMedicineBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "AboutAddMedicineBottomSheet");
        });

        // 1. UI Components
        medicineNameInput = view.findViewById(R.id.medicineNameInput);
        quantityInput = view.findViewById(R.id.quantityInput);
        typeSpinner = view.findViewById(R.id.medicineTypeSpinner);
        unitSpinner = view.findViewById(R.id.medicineUnitSpinner);
        dosageSpinner = view.findViewById(R.id.medicineDosageSpinner);
        frequencySpinner = view.findViewById(R.id.medicineFrequencySpinner);
        startDateText = view.findViewById(R.id.startDateText);
        timeText = view.findViewById(R.id.timeText);
        reminderSwitch = view.findViewById(R.id.reminderSwitch);
        TextView tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
        TextView saveBtn = view.findViewById(R.id.saveBtn);

        // 2. Medicine Type Dropdown
        String[] medicineTypes = {"Tablet", "Capsule", "Syrup", "Liquid", "Injection", "Ointment", "Drops", "Inhaler", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, medicineTypes);
        typeSpinner.setAdapter(typeAdapter);

        // 3. Unit Dropdown
        String[] medicineUnits = {"Tablets", "Capsules", "ml", "Drops", "mg", "g", "Puffs", "Units"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, medicineUnits);
        unitSpinner.setAdapter(unitAdapter);

        // Smart Linkage
        typeSpinner.setOnItemClickListener((parent, v, position, id) -> {
            String selectedType = (String) parent.getItemAtPosition(position);
            ImageView medIcon = view.findViewById(R.id.medThumbnail);
            medIcon.setImageTintList(null);

            switch (selectedType) {
                case "Tablet": 
                    unitSpinner.setText("Tablets", false);
                    selectedIconResId = R.drawable.image_01;
                    break;
                case "Capsule": 
                    unitSpinner.setText("Capsules", false);
                    selectedIconResId = R.drawable.image_02;
                    break;
                case "Syrup":
                    unitSpinner.setText("ml", false);
                    selectedIconResId = R.drawable.image_03;
                    break;
                case "Liquid": 
                    unitSpinner.setText("ml", false);
                    selectedIconResId = R.drawable.image_04;
                    break;
                case "Injection":
                    selectedIconResId = R.drawable.inj_image_010;
                    break;
                case "Ointment":
                    selectedIconResId = R.drawable.image_06;
                    break;
                case "Drops": 
                    unitSpinner.setText("Drops", false);
                    selectedIconResId = R.drawable.image_05;
                    break;
                case "Inhaler": 
                    unitSpinner.setText("Puffs", false);
                    selectedIconResId = R.drawable.med_image_08;
                    break;
                case "Other":
                    selectedIconResId = R.drawable.medsec_image_09;
                    break;
            }
            medIcon.setImageResource(selectedIconResId);
        });

        // 4. Dosage & Frequency
        String[] dosageOptions = {"1", "2", "3", "0.5", "1.5", "5", "10"};
        ArrayAdapter<String> dosageAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, dosageOptions);
        dosageSpinner.setAdapter(dosageAdapter);

        String[] frequencies = {"Once a day", "Twice a day", "3 times a day", "4 times a day", "Every 8 hours", "Every 12 hours", "Specific days", "Custom"};
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, frequencies);
        frequencySpinner.setAdapter(freqAdapter);

        // Pre-fill if editing
        if (existingMedicine != null) {
            tvHeaderTitle.setText("Edit Medicine");
            saveBtn.setText("Update Medicine");
            medicineNameInput.setText(existingMedicine.getMedicineName());
            typeSpinner.setText(existingMedicine.getMedicineType(), false);
            quantityInput.setText(existingMedicine.getQuantity());
            unitSpinner.setText(existingMedicine.getUnit(), false);
            dosageSpinner.setText(existingMedicine.getDosage(), false);
            frequencySpinner.setText(existingMedicine.getFrequency(), false);
            startDateText.setText(existingMedicine.getStartDate());
            timeText.setText(existingMedicine.getStartTime());
            reminderSwitch.setChecked(existingMedicine.isReminderOn());
            selectedIconResId = existingMedicine.getIconResId();
            
            ImageView medIcon = view.findViewById(R.id.medThumbnail);
            medIcon.setImageResource(selectedIconResId);
            medIcon.setImageTintList(null);
        }

        // 5. Date and Time Pickers
        view.findViewById(R.id.startDateBtn).setOnClickListener(v -> showDatePicker(view));
        view.findViewById(R.id.timePickerBtn).setOnClickListener(v -> showTimePicker(view));

        // 6. Save Button
        view.findViewById(R.id.saveBtn).setOnClickListener(v -> saveMedicine());

        return view;
    }

    private void showDatePicker(View view) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (d, year, month, day) -> {
            TextView dateText = view.findViewById(R.id.startDateText);
            dateText.setText(day + "/" + (month + 1) + "/" + year);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(View view) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (t, hour, minute) -> {
            TextView timeText = view.findViewById(R.id.timeText);
            String amPm = hour >= 12 ? "PM" : "AM";
            int displayHour = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
            timeText.setText(String.format("%02d:%02d %s", displayHour, minute, amPm));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
    }

    private void saveMedicine() {
        String name = medicineNameInput.getText().toString();
        if (name.isEmpty()){
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Please enter medicine name", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        MedicineEntity med = (existingMedicine != null) ? existingMedicine : new MedicineEntity();
        med.setMedicineName(name);
        med.setMedicineType(typeSpinner.getText().toString());
        med.setQuantity(quantityInput.getText().toString());
        med.setUnit(unitSpinner.getText().toString());
        med.setDosage(dosageSpinner.getText().toString());
        med.setFrequency(frequencySpinner.getText().toString());
        med.setStartDate(startDateText.getText().toString());
        med.setStartTime(timeText.getText().toString());
        med.setReminderOn(reminderSwitch.isChecked());
        med.setIconResId(selectedIconResId);

        Context context = getContext();
        if (context == null) return;
        new Thread(() -> {
            if (existingMedicine != null) {
                AppDatabase.getInstance(context).medicineDao().update(med);
            } else {
                long id = AppDatabase.getInstance(context).medicineDao().insert(med);
                med.setId((int) id);
            }

            // Schedule notification if reminder is on
            if (med.isReminderOn()) {
                scheduleMedicineNotification(context, med);
            } else {
                NotificationHelper.cancelNotification(context, med.getId());
            }
            
            if (isAdded()) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(context, existingMedicine != null ? "Medicine Updated!" : "Medicine Saved!", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            }
        }).start();
    }

    private void scheduleMedicineNotification(Context context, MedicineEntity med) {
        try {
            String dateTimeStr = med.getStartDate() + " " + med.getStartTime();
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy hh:mm a", Locale.getDefault());
            Date date = sdf.parse(dateTimeStr);
            if (date != null) {
                long timeInMillis = date.getTime();
                // If the time has already passed today, we don't schedule or we could schedule for tomorrow
                // For simplicity, we just schedule for the given time.
                NotificationHelper.scheduleNotification(context, timeInMillis, 
                        "Medicine Reminder: " + med.getMedicineName(),
                        "Time to take your " + med.getDosage() + " " + med.getUnit(),
                        med.getId());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
