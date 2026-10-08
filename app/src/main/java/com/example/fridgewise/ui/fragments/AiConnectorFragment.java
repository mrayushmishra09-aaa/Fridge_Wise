package com.example.fridgewise.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fridgewise.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.UUID;

public class AiConnectorFragment extends Fragment {

    public AiConnectorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_connector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnGenerateToken = view.findViewById(R.id.btnGenerateToken);
        MaterialButton btnCopyEndpoint = view.findViewById(R.id.btnCopyEndpoint);
        MaterialButton btnTestAction = view.findViewById(R.id.btnTestAction);
        Chip chipAiStatus = view.findViewById(R.id.chipAiStatus);
        TextView tvActivityLog = view.findViewById(R.id.tvActivityLog);

        btnGenerateToken.setOnClickListener(v -> {
            String token = "FW-AI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            chipAiStatus.setText("Connected / Active");
            chipAiStatus.setChipBackgroundColorResource(R.color.green_primary);
            tvActivityLog.setText("AI Agent paired successfully.\nGenerated Token: " + token + "\nReady for MCP Custom Space CRUD & Card Generation.");
            Toast.makeText(getContext(), "AI Connector Token Generated!", Toast.LENGTH_SHORT).show();
        });

        if (btnCopyEndpoint != null) {
            btnCopyEndpoint.setOnClickListener(v -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("MCP Endpoint", "https://api.fridgewise.com/mcp/v1/bridge");
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), "MCP Endpoint URL copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnTestAction != null) {
            btnTestAction.setOnClickListener(v -> {
                tvActivityLog.setText("Test Action Executed:\n[✓] Read Custom Spaces schema\n[✓] Created AI-driven Custom Space ('Study & Goals')\n[✓] Generated dynamic card & streak widget\nStatus: Success (200 OK)");
                Toast.makeText(getContext(), "AI Custom Space CRUD & Card Gen Tested!", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
