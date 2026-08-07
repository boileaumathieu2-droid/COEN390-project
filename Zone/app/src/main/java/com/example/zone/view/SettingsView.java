package com.example.zone.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.zone.R;
import com.example.zone.model.Database;
import com.example.zone.model.Session;
import com.example.zone.model.StudySessionModel;
import com.example.zone.model.TimerModel;
import com.example.zone.model.VirtualDatabase;

public class SettingsView extends Fragment {
    private final VirtualDatabase db = new VirtualDatabase();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_menu, container, false);
        
        view.findViewById(R.id.connectDeviceButton).setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), HeartRateMonitorView.class)));
        view.findViewById(R.id.appRestrictButton).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), BlockedAppsView.class)));
        // Notification silencing now lives inside Focus Restrictions so both
        // interruption controls are managed from one place.
        view.findViewById(R.id.notificationsButton).setVisibility(View.GONE);
        view.findViewById(R.id.aboutHelpButton).setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), AboutHelpView.class)));
        view.findViewById(R.id.logoutButton).setOnClickListener(v -> logout());
        view.findViewById(R.id.deleteAccountButton).setOnClickListener(v ->
                confirmDeleteAccount());
        
        return view;
    }

    private void logout() {
        TimerModel.getInstance().stopAndReset();
        StudySessionModel.reset();
        db.signOut();
        Session.logout();
        Intent intent = new Intent(requireContext(), LoginView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show();
        if (getActivity() != null) getActivity().finish();
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete your account?")
                .setMessage("This permanently removes your Zone account, tasks, subjects, grades and study history. This cannot be undone.")
                .setPositiveButton("Delete account", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        String username = Session.getUsername();
        Toast.makeText(requireContext(), "Deleting account…", Toast.LENGTH_SHORT).show();
        db.deleteAccount((success, message) -> {
            if (!isAdded()) {
                return;
            }
            if (!success) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                return;
            }

            new Database(requireContext()).deleteUserAndLocalData(username);
            TimerModel.getInstance().stopAndReset();
            StudySessionModel.reset();
            Session.logout();

            Intent intent = new Intent(requireContext(), LoginView.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
}
