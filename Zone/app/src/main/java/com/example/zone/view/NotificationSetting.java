package com.example.zone.view;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
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
import com.example.zone.controller.StudyReminderScheduler;
import com.example.zone.model.NotificationPreferences;

public class NotificationSetting extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notification Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SwitchCompat reminders = findViewById(R.id.switchStudyReminders);
        SwitchCompat complete = findViewById(R.id.switchSessionComplete);
        SwitchCompat breaks = findViewById(R.id.switchBreakReminders);
        SwitchCompat mute = findViewById(R.id.switchMuteDuringStudy);

        reminders.setChecked(NotificationPreferences.studyRemindersEnabled(this));
        complete.setChecked(NotificationPreferences.sessionCompleteEnabled(this));
        breaks.setChecked(NotificationPreferences.breakRemindersEnabled(this));
        mute.setChecked(NotificationPreferences.muteDuringStudyEnabled(this));

        reminders.setOnCheckedChangeListener((button, checked) -> {
            NotificationPreferences.setStudyRemindersEnabled(this, checked);
            if (checked) {
                requestNotificationPermissionIfNeeded();
                StudyReminderScheduler.schedule(this);
            } else {
                StudyReminderScheduler.cancel(this);
            }
        });
        complete.setOnCheckedChangeListener((button, checked) -> {
            NotificationPreferences.setSessionCompleteEnabled(this, checked);
            if (checked) {
                requestNotificationPermissionIfNeeded();
            }
        });
        breaks.setOnCheckedChangeListener((button, checked) -> {
            NotificationPreferences.setBreakRemindersEnabled(this, checked);
            if (checked) {
                requestNotificationPermissionIfNeeded();
            }
        });
        mute.setOnCheckedChangeListener((button, checked) -> {
            NotificationPreferences.setMuteDuringStudyEnabled(this, checked);
            if (checked) {
                requestDndAccessIfNeeded();
            }
        });

        if (reminders.isChecked()) {
            requestNotificationPermissionIfNeeded();
            StudyReminderScheduler.schedule(this);
        }
        if (complete.isChecked() || breaks.isChecked()) {
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
