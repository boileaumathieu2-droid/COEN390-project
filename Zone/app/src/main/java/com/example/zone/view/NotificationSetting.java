package com.example.zone.view;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zone.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class NotificationSetting extends AppCompatActivity {
    private SwitchCompat sendNotifications;
    private SwitchCompat sessionComplete;
    private SwitchCompat breakReminders;
    private SwitchCompat muteDuringStudy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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
    }
}







