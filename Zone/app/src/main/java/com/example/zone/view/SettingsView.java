package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.model.VirtualDatabase;

public class SettingsView extends AppCompatActivity {
    private VirtualDatabase db = new VirtualDatabase();
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.settings_menu);
        TextView logout = findViewById(R.id.logoutButton);
        TextView connectDevice = findViewById(R.id.connectDeviceButton);
        TextView appRestrict = findViewById(R.id.appRestrictButton);
        TextView Notifications = findViewById(R.id.notificationsButton);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        logout.setOnClickListener(v -> {
            db.signOut();
            Intent intent = new Intent(this, LoginView.class);
            startActivity(intent);
            Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show();
        });

        connectDevice.setOnClickListener(v -> {
            Intent intent = new Intent(this, HeartRateMonitorView.class);
            startActivity(intent);
        });

        appRestrict.setOnClickListener(v -> {
            Intent intent = new Intent(this, BlockedAppsView.class);
            startActivity(intent);
        });
        Notifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationSetting.class);
            startActivity(intent);
        });
    }
}
