package com.example.zone.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.content.Intent;
import com.example.zone.model.TimerModel;
import androidx.core.view.WindowInsetsCompat;
import com.example.zone.controller.TimerSettingsController;
import android.widget.EditText;
import android.widget.TextView;
import com.example.zone.R;


public class TimerSettingsView extends AppCompatActivity{

    // define variables for the user input
    EditText studyMinutes;
    EditText studySeconds;
    EditText breakMinutes;
    EditText breakSeconds;

    private TimerSettingsController timerSettingsController;
    private TimerModel timerModel;
    private androidx.appcompat.widget.SwitchCompat breakTimerSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.timer_settings);

        timerModel = TimerModel.getInstance();

        // get the user inputs inside variables
        studyMinutes = findViewById(R.id.edit_study_minutes);
        studySeconds = findViewById(R.id.edit_study_seconds);
        breakMinutes = findViewById(R.id.edit_break_minutes);
        breakSeconds = findViewById(R.id.edit_break_seconds);
        breakTimerSwitch = findViewById(R.id.switch_break_timer);

        android.view.View rootView = findViewById(R.id.timer);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        timerSettingsController = new TimerSettingsController(this);
        loadSavedValues();
    }


    private void loadSavedValues() {
        TimerModel model = TimerModel.getInstance();

        int studyMin = model.getStudyDuration() / 60;
        int studySec = model.getStudyDuration() % 60;
        int breakMin = model.getBreakDuration() / 60;
        int breakSec = model.getBreakDuration() % 60;

        // Restore switch state
        breakTimerSwitch.setChecked(model.isBreakEnabled());

        // Only show non-zero saved values (optional — avoids showing "0" on first launch)
        if (model.getStudyDuration() > 0) {
            studyMinutes.setText(String.valueOf(studyMin));
            studySeconds.setText(String.valueOf(studySec));
        }
        if (model.getBreakDuration() > 0) {
            breakMinutes.setText(String.valueOf(breakMin));
            breakSeconds.setText(String.valueOf(breakSec));
        }
    }

    // Functions here return the user input from the text fields
    public int getStudyMins() {
        return parseOrDefault(studyMinutes.getText().toString(), "smin");
    }

    public int getStudySecs() {
        return parseOrDefault(studySeconds.getText().toString(), "ssec");
    }

    public int getBreakMins() {
        return parseOrDefault(breakMinutes.getText().toString(), "bmin");
    }

    public int getBreakSecs() {
        return parseOrDefault(breakSeconds.getText().toString(), "bsec");
    }


    public boolean isBreakTimerEnabled() {
        return breakTimerSwitch != null && breakTimerSwitch.isChecked();
    }

    // take care of null input case
    private int parseOrDefault(String text, String type) {
        if (text == null || text.trim().isEmpty()) {
            // default time for each case
            switch (type) {
                case "smin":
                    return 25;
                case "ssec":
                    return 0;
                case "bmin":
                    return 5;
                case "bsec":
                    return 0;
                default:
                    return 0;
            }
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
