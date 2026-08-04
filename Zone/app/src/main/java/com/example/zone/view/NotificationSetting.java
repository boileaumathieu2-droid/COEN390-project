package com.example.zone.view;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.zone.R;

public class NotificationSetting extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 101;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notification Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        SwitchCompat reminders = findViewById(R.id.switchStudyReminders);
        SwitchCompat complete = findViewById(R.id.switchSessionComplete);
        SwitchCompat breaks = findViewById(R.id.switchBreakReminders);
        SwitchCompat mute = findViewById(R.id.switchMuteDuringStudy);

        reminders.setChecked(preferences.getBoolean("Notifications", false));
        complete.setChecked(preferences.getBoolean("temporary", false));
        breaks.setChecked(preferences.getBoolean("alsoTemp", false));
        mute.setChecked(preferences.getBoolean("Mute", false));

        reminders.setOnCheckedChangeListener((button, checked) -> {
            preferences.edit().putBoolean("Notifications", checked).apply();
            if (checked) {
                requestNotificationPermissionIfNeeded();
            }
        });
        complete.setOnCheckedChangeListener((button, checked) ->
                preferences.edit().putBoolean("temporary", checked).apply());
        breaks.setOnCheckedChangeListener((button, checked) ->
                preferences.edit().putBoolean("alsoTemp", checked).apply());
        mute.setOnCheckedChangeListener((button, checked) -> {
            preferences.edit().putBoolean("Mute", checked).apply();
            if (checked) {
                requestDndAccessIfNeeded();
            }
        });

        if (reminders.isChecked()) {
            requestNotificationPermissionIfNeeded();
        }
        if (mute.isChecked()) {
            requestDndAccessIfNeeded();
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATION_PERMISSION_REQUEST
        );
    }

    private void requestDndAccessIfNeeded() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null && manager.isNotificationPolicyAccessGranted()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Do Not Disturb access")
                .setMessage("Open Android settings to allow Zone to mute interruptions during a study session?")
                .setPositiveButton("Open settings", (dialog, which) ->
                        startActivity(new Intent(
                                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                        )))
                .setNegativeButton("Not now", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
