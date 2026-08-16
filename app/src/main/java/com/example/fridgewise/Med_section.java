package com.example.fridgewise;

import android.content.Context;
import android.os.Bundle;

import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Med_section#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Med_section extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MedicineAdapter adaptor;
    private List<MedicineEntity> medicineList = new ArrayList<>();
    private TextView tvProgressDoses;
    private LinearProgressIndicator progressDoses;

    public Med_section() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Med_section.
     */
    // TODO: Rename and change types and number of parameters
    public static Med_section newInstance(String param1, String param2) {
        Med_section fragment = new Med_section();
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
        View view = inflater.inflate(R.layout.fragment_med_section, container, false);

        ImageView backBtn = view.findViewById(R.id.backBtn_medsec);
        backBtn.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        View btnAddMedicine = view.findViewById(R.id.btnAddMedicine);
        btnAddMedicine.setOnClickListener( v->{
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, new MedicineAddFragment())
                    .addToBackStack(null)
                    .commit();
        });

        View btnInfo = view.findViewById(R.id.btnInfo_med);
        btnInfo.setOnClickListener(v -> {
            AboutMedicineBottomSheet bottomSheet = new AboutMedicineBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "AboutMedicineBottomSheet");
        });

        RecyclerView rvMedicine = view.findViewById(R.id.rvMedicine);
        rvMedicine.setLayoutManager(new LinearLayoutManager(requireContext()));
        adaptor = new MedicineAdapter(medicineList, new MedicineAdapter.OnMedicineClickListener() {
            @Override
            public void onReminderToggle(MedicineEntity medicine, boolean isChecked) {
                medicine.setReminderOn(isChecked);
                updateMedicine(medicine);
            }

            @Override
            public void onTakeDose(MedicineEntity medicine) {
                logDose(medicine);
            }

            @Override
            public void onEditClick(MedicineEntity medicine) {
                MedicineAddFragment editFragment = new MedicineAddFragment();
                Bundle args = new Bundle();
                args.putSerializable(MedicineAddFragment.ARG_MEDICINE, medicine);
                editFragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView2, editFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDeleteClick(MedicineEntity medicine) {
                deleteMedicine(medicine);
            }
        });
        rvMedicine.setAdapter(adaptor);

        tvProgressDoses = view.findViewById(R.id.tvProgressDoses);
        progressDoses = view.findViewById(R.id.progressDoses);

        loadMedicines();

        return view;
    }

    private void logDose(MedicineEntity medicine) {
        Context context = getContext();
        if (context == null) return;

        String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());
        medicine.setLastTakenDate(today);

        // Update inventory
        try {
            double currentQty = Double.parseDouble(medicine.getQuantity());
            double dosage = Double.parseDouble(medicine.getDosage());
            if (currentQty >= dosage) {
                medicine.setQuantity(String.valueOf(currentQty - dosage));
            }
        } catch (Exception e) {
            // If parsing fails, we just don't update quantity
        }

        new Thread(() -> {
            AppDatabase.getInstance(context).medicineDao().update(medicine);
            if (isAdded()) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(context, "Dose logged for " + medicine.getMedicineName(), Toast.LENGTH_SHORT).show();
                    loadMedicines(); // Refresh UI
                });
            }
        }).start();
    }

    private void updateMedicine(MedicineEntity medicine) {
        Context context = getContext();
        if (context == null) return;
        new Thread(() -> {
            AppDatabase.getInstance(context).medicineDao().update(medicine);
        }).start();
    }

    private void deleteMedicine(MedicineEntity medicine) {
        Context context = getContext();
        if (context == null) return;
        new Thread(() -> {
            AppDatabase.getInstance(context).medicineDao().delete(medicine);
            if (isAdded()) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(context, "Medicine deleted", Toast.LENGTH_SHORT).show();
                    loadMedicines();
                });
            }
        }).start();
    }

    private void loadMedicines() {
        Context context = getContext();
        if (context == null) return;
        new Thread(() -> {
            List<MedicineEntity> list = AppDatabase.getInstance(context).medicineDao().getAllMedicines();
            if (isAdded()) {
                getActivity().runOnUiThread(() -> {
                    medicineList.clear();
                    medicineList.addAll(list);
                    adaptor.notifyDataSetChanged();
                    updateProgress();
                });
            }
        }).start();
    }

    private void updateProgress() {
        if (medicineList == null || tvProgressDoses == null || progressDoses == null) return;

        int totalDoses = medicineList.size();
        int takenDoses = 0;
        String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());

        for (MedicineEntity medicine : medicineList) {
            if (today.equals(medicine.getLastTakenDate())) {
                takenDoses++;
            }
        }

        tvProgressDoses.setText(takenDoses + " of " + totalDoses + " doses taken");
        if (totalDoses > 0) {
            int progress = (int) ((takenDoses / (float) totalDoses) * 100);
            progressDoses.setProgress(progress, true);
        } else {
            progressDoses.setProgress(0, true);
        }
    }
}
