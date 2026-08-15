package com.example.fridgewise;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

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

        MaterialButton btnAddMedicine = view.findViewById(R.id.btnAddMedicine);
        btnAddMedicine.setOnClickListener( v->{
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, new MedicineAddFragment())
                    .addToBackStack(null)
                    .commit();
        });

        RecyclerView rvMedicine = view.findViewById(R.id.rvMedicine);
        rvMedicine.setLayoutManager(new LinearLayoutManager(requireContext()));
        adaptor = new MedicineAdapter(medicineList, (medicine, isChecked) -> {
            medicine.setReminderOn(isChecked);
            updateMedicine(medicine);
        });
        rvMedicine.setAdapter(adaptor);

        loadMedicines();

        return view;
    }

    private void updateMedicine(MedicineEntity medicine) {
        Context context = getContext();
        if (context == null) return;
        new Thread(() -> {
            AppDatabase.getInstance(context).medicineDao().update(medicine);
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
                });
            }
        }).start();
    }
}
