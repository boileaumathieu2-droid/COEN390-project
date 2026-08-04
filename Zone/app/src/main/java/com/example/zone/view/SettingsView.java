package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.model.Session;
import com.example.zone.model.TimerModel;
import com.example.zone.model.VirtualDatabase;

public class SettingsView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);
        Session.init(getApplicationContext());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.connectDeviceButton).setOnClickListener(view ->
                startActivity(new Intent(this, HeartRateMonitorView.class)));
        findViewById(R.id.appRestrictButton).setOnClickListener(view ->
                startActivity(new Intent(this, BlockedAppsView.class)));
        findViewById(R.id.notificationsButton).setOnClickListener(view ->
                startActivity(new Intent(this, NotificationSetting.class)));
        findViewById(R.id.aboutHelpButton).setOnClickListener(view ->
                startActivity(new Intent(this, AboutHelpView.class)));
        findViewById(R.id.logoutButton).setOnClickListener(view -> logout());
    }

    private void logout() {
        TimerModel.getInstance().stopAndReset();
        new VirtualDatabase().signOut();
        Session.logout();
        Intent intent = new Intent(this, LoginView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
