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

        EdgeToEdge.enable(this);
        setContentView(R.layout.timer_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Configure Timer");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        applyWindowInsets();

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

    private void applyWindowInsets() {
        View rootView = findViewById(R.id.timer);

        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    rootView,
                    (view, windowInsets) -> {
                        Insets systemBars = windowInsets.getInsets(
                                WindowInsetsCompat.Type.systemBars()
                        );

                        view.setPadding(
                                systemBars.left,
                                systemBars.top,
                                systemBars.right,
                                systemBars.bottom
                        );

                        return windowInsets;
                    }
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem option) {
        if (option.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsView.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(option);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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