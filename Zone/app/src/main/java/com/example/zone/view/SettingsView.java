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
import androidx.fragment.app.Fragment;

import com.example.zone.R;
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
        view.findViewById(R.id.notificationsButton).setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), NotificationSetting.class)));
        view.findViewById(R.id.aboutHelpButton).setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), AboutHelpView.class)));
        view.findViewById(R.id.logoutButton).setOnClickListener(v -> logout());
        
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
}
