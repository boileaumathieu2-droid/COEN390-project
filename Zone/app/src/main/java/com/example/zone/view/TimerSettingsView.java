package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zone.R;
import com.example.zone.controller.TimerSettingsController;

public class TimerSettingsView extends AppCompatActivity {

    private EditText studyMinutes;
    private EditText studySeconds;
    private EditText breakMinutes;
    private EditText breakSeconds;
    private SwitchCompat breakTimerSwitch;

    private TimerSettingsController timerSettingsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.timer_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Configure Timer");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();

        timerSettingsController = new TimerSettingsController(this, this);
        timerSettingsController.initializeView();
    }

    private void initializeViews() {
        studyMinutes = findViewById(R.id.edit_study_minutes);
        studySeconds = findViewById(R.id.edit_study_seconds);
        breakMinutes = findViewById(R.id.edit_break_minutes);
        breakSeconds = findViewById(R.id.edit_break_seconds);
        breakTimerSwitch = findViewById(R.id.switch_break_timer);
    }




    // View setters called by TimerSettingsController

    public void setStudyMins(String minutes) {
        studyMinutes.setText(minutes);
    }

    public void setStudySecs(String seconds) {
        studySeconds.setText(seconds);
    }

    public void setBreakMins(String minutes) {
        breakMinutes.setText(minutes);
    }

    public void setBreakSecs(String seconds) {
        breakSeconds.setText(seconds);
    }

    public void setBreakEnabled(boolean enabled) {
        breakTimerSwitch.setChecked(enabled);
    }

    // View getters called by TimerSettingsController

    public String getStudyMinsText() {
        return studyMinutes.getText().toString();
    }

    public String getStudySecsText() {
        return studySeconds.getText().toString();
    }

    public String getBreakMinsText() {
        return breakMinutes.getText().toString();
    }

    public String getBreakSecsText() {
        return breakSeconds.getText().toString();
    }

    public boolean isBreakTimerEnabled() {
        return breakTimerSwitch != null
                && breakTimerSwitch.isChecked();
    }
}
