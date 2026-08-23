package com.example.fridgewise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class IntroFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_DESC_SEC = "desc_sec";
    private static final String ARG_IMAGE = "image";

    public IntroFragment() {
    }

    public static IntroFragment newInstance(String title, String desc, int imageResId) {
        return newInstance(title, desc, null, imageResId);
    }

    public static IntroFragment newInstance(String title, String desc, String descSec, int imageResId) {
        IntroFragment fragment = new IntroFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESC, desc);
        args.putString(ARG_DESC_SEC, descSec);
        args.putInt(ARG_IMAGE, imageResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro, container, false);

        TextView tvTitle = view.findViewById(R.id.tvIntroTitle);
        TextView tvDesc = view.findViewById(R.id.tvIntroDescription);
        TextView tvDescSec = view.findViewById(R.id.tvIntroDescriptionSecondary);
        ImageView ivImage = view.findViewById(R.id.ivIntroImage);
        View detailSection = view.findViewById(R.id.ll_detail_section);

        if (getArguments() != null) {
            tvTitle.setText(getArguments().getString(ARG_TITLE));
            tvDesc.setText(getArguments().getString(ARG_DESC));
            ivImage.setImageResource(getArguments().getInt(ARG_IMAGE));

            String descSec = getArguments().getString(ARG_DESC_SEC);
            if (descSec != null && !descSec.isEmpty()) {
                tvDescSec.setText(descSec);
                detailSection.setVisibility(View.VISIBLE);
            } else {
                detailSection.setVisibility(View.GONE);
            }
        }

        return view;
    }
}
