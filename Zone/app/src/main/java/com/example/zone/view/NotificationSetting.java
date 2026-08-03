package com.example.zone.view;


import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zone.R;
import com.example.zone.controller.NotificationController;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class NotificationSetting extends AppCompatActivity {

    private SwitchCompat sendNotifications;
    private SwitchCompat sessionComplete;
    private SwitchCompat breakReminders;
    private SwitchCompat muteDuringStudy;
    private SharedPreferences prefs;

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        setContentView(R.layout.activity_notification_setting);
        sendNotifications = findViewById(R.id.switchStudyReminders);
        sessionComplete = findViewById(R.id.switchSessionComplete);
        breakReminders = findViewById(R.id.switchBreakReminders);
        muteDuringStudy = findViewById(R.id.switchMuteDuringStudy);

        muteDuringStudy.setChecked(prefs.getBoolean("Mute", false));
        sendNotifications.setChecked(prefs.getBoolean("Notifications", false));
        sessionComplete.setChecked(prefs.getBoolean("temporary", false));
        breakReminders.setChecked(prefs.getBoolean("alsoTemp", false));
        hasDndAccess();
        muteDuringStudy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Mute", isChecked);
            editor.apply();
        });
        sendNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Notifications", isChecked);
            editor.apply();
        });
        breakReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("alsoTemp", isChecked);
            editor.apply();
        });
        sessionComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("temporary", isChecked);
            editor.apply();
        });
        VerifyPermission(this);
    }

    private void hasDndAccess() {
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean mute = prefs.getBoolean("Mute", false);
        NotificationManager notificationManager =
                getSystemService(NotificationManager.class);
        boolean x = notificationManager != null
                && notificationManager.isNotificationPolicyAccessGranted();
        if (mute && !x) {
            getDnDPermission();
        }
    }

    private void getDnDPermission() {
        new AlertDialog.Builder(this)
                .setTitle("PERMISSION NOT GRANTED")
                .setMessage("Would you permission to enable DnD?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    public void VerifyPermission(Context context) {
        prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean Notifications = prefs.getBoolean("Notifications", false);
        boolean x = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            x = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        if( !x && Notifications) {
            GetNotificationPermission(context);
        }
    }
    public void GetNotificationPermission(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("PERMISSION NOT GRANTED")
                .setMessage("Would you like to enable notifications?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    context.startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}

















