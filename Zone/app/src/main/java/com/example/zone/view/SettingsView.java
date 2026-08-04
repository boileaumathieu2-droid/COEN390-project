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
import com.example.zone.model.VirtualDatabase;

public class SettingsView extends Fragment {
    private VirtualDatabase db = new VirtualDatabase();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_menu, container, false);
        
        TextView logout = view.findViewById(R.id.logoutButton);
        TextView connectDevice = view.findViewById(R.id.connectDeviceButton);
        TextView appRestrict = view.findViewById(R.id.appRestrictButton);
        TextView Notifications = view.findViewById(R.id.notificationsButton);

        logout.setOnClickListener(v -> {
            db.signOut();
            startActivity(new Intent(requireContext(), LoginView.class));
            Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().finish();
        });

        connectDevice.setOnClickListener(v -> startActivity(new Intent(requireContext(), HeartRateMonitorView.class)));
        appRestrict.setOnClickListener(v -> startActivity(new Intent(requireContext(), BlockedAppsView.class)));
        Notifications.setOnClickListener(v -> startActivity(new Intent(requireContext(), NotificationSetting.class)));
        
        return view;
    }
}
