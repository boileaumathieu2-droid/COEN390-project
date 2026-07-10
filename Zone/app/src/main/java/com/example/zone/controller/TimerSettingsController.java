package com.example.zone.controller;

import android.widget.Button;
import android.widget.CompoundButton;
import com.example.zone.model.TimerModel;
import com.example.zone.R;
import com.example.zone.view.TimerSettingsView;

public class TimerSettingsController {

    private TimerSettingsView timerSettingsView;
    private TimerModel timerModel;

    public TimerSettingsController(TimerSettingsView activity) {
        this.timerSettingsView = activity;
        this.timerModel = TimerModel.getInstance();

        // Link the switch to the toggle logic
        CompoundButton breakTimerSwitch = activity.findViewById(R.id.switch_break_timer);
        if (breakTimerSwitch != null) {
            // TODO: Make sure that the switch has memory
            breakTimerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onBreakTimerToggled(isChecked));
        }

        // Link buttons to their actions
        Button saveButton = activity.findViewById(R.id.button_save_settings);
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> saveSettings());
        }

        Button cancelButton = activity.findViewById(R.id.button_cancel_settings);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> cancelSettings());
        }
    }

    public void onBreakTimerToggled(boolean isChecked) {
        int visibility = isChecked ? android.view.View.VISIBLE : android.view.View.GONE;
        
        android.view.View breakLabel = timerSettingsView.findViewById(R.id.text_break_label);
        android.view.View breakMinLabel = timerSettingsView.findViewById(R.id.label_break_minutes);
        android.view.View breakMin = timerSettingsView.findViewById(R.id.edit_break_minutes);
        android.view.View breakSecLabel = timerSettingsView.findViewById(R.id.label_break_seconds);
        android.view.View breakSec = timerSettingsView.findViewById(R.id.edit_break_seconds);

        if (breakLabel != null) breakLabel.setVisibility(visibility);
        if (breakMinLabel != null) breakMinLabel.setVisibility(visibility);
        if (breakMin != null) breakMin.setVisibility(visibility);
        if (breakSecLabel != null) breakSecLabel.setVisibility(visibility);
        if (breakSec != null) breakSec.setVisibility(visibility);
    }

    public void saveSettings() {
        // save the total study time in the class
        int studyTotal = timerSettingsView.getStudyMins() * 60 + timerSettingsView.getStudySecs();
        timerModel.setStudyDuration(studyTotal);

        // save the break time and its enabled status
        int breakTotal = timerSettingsView.getBreakMins() * 60 + timerSettingsView.getBreakSecs();
        timerModel.setBreakDuration(breakTotal);
        timerModel.setBreakEnabled(timerSettingsView.isBreakTimerEnabled());

        timerSettingsView.finish(); // go back to main menu
    }

    public void cancelSettings() {
        // leave without saving
        timerSettingsView.finish();
    }
}