package com.example.zone.controller;

import android.content.Context;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import com.example.zone.model.TimerModel;
import com.example.zone.R;
import com.example.zone.view.TimerSettingsView;

public class TimerSettingsController {

    private TimerSettingsView timerSettingsView;
    private TimerModel timerModel;

    public TimerSettingsController(TimerSettingsView activity, Context context) {
        this.timerSettingsView = activity;
        this.timerModel = TimerModel.getInstance(context);

        // Link the switch to the toggle logic
        CompoundButton breakTimerSwitch = activity.findViewById(R.id.switch_break_timer);
        if (breakTimerSwitch != null) {
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

    /**
     * Controller function: Loads data from Model and populates the View.
     */
    public void initializeView() {
        int studyMin = timerModel.getStudyDuration() / 60;
        int studySec = timerModel.getStudyDuration() % 60;
        int breakMin = timerModel.getBreakDuration() / 60;
        int breakSec = timerModel.getBreakDuration() % 60;

        // Restore switch state
        timerSettingsView.setBreakEnabled(timerModel.isBreakEnabled());
        onBreakTimerToggled(timerModel.isBreakEnabled()); // Manually trigger visibility update

        // Display current saved values
        timerSettingsView.setStudyMins(String.valueOf(studyMin));
        timerSettingsView.setStudySecs(String.valueOf(studySec));

        if (timerModel.getBreakDuration() > 0) {
            timerSettingsView.setBreakMins(String.valueOf(breakMin));
            timerSettingsView.setBreakSecs(String.valueOf(breakSec));
        }
    }

    /**
     * Controller function: Handles the UI visibility logic based on switch state.
     */
    public void onBreakTimerToggled(boolean isChecked) {
        int visibility = isChecked ? android.view.View.VISIBLE : android.view.View.GONE;

        android.view.View breakRow = timerSettingsView.findViewById(R.id.break_time_row);
        android.view.View breakLabel = timerSettingsView.findViewById(R.id.text_break_label);
        android.view.View breakMinLabel = timerSettingsView.findViewById(R.id.label_break_minutes);
        android.view.View breakMin = timerSettingsView.findViewById(R.id.edit_break_minutes);
        android.view.View breakSecLabel = timerSettingsView.findViewById(R.id.label_break_seconds);
        android.view.View breakSec = timerSettingsView.findViewById(R.id.edit_break_seconds);
        
        if (breakRow != null) breakRow.setVisibility(visibility);
        if (breakLabel != null) breakLabel.setVisibility(visibility);
        if (breakMinLabel != null) breakMinLabel.setVisibility(visibility);
        if (breakMin != null) breakMin.setVisibility(visibility);
        if (breakSecLabel != null) breakSecLabel.setVisibility(visibility);
        if (breakSec != null) breakSec.setVisibility(visibility);
    }

    /**
     * Controller function: Gathers data from View, validates/parses it, and updates the Model.
     */
    public void saveSettings() {
        int studyMins = parseInput(timerSettingsView.getStudyMinsText());
        int studySecs = parseInput(timerSettingsView.getStudySecsText());
        int breakMins = parseInput(timerSettingsView.getBreakMinsText());
        int breakSecs = parseInput(timerSettingsView.getBreakSecsText());

        int studyTotal = (studyMins * 60) + studySecs;
        int breakTotal = (breakMins * 60) + breakSecs;

        if (studyTotal <= 0) {
            Toast.makeText(timerSettingsView,
                    "Enter a study time greater than 0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        boolean breakEnabled = timerSettingsView.isBreakTimerEnabled();
        if (breakEnabled && breakTotal <= 0) {
            Toast.makeText(timerSettingsView,
                    "Enter a break time greater than 0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        timerModel.stopAndReset();
        timerModel.setStudyDuration(studyTotal);
        timerModel.setBreakDuration(breakTotal);
        timerModel.setBreakEnabled(breakEnabled);
        timerSettingsView.finish();
    }

    public void cancelSettings() {
        timerSettingsView.finish();
    }

    /**
     * Controller helper: Logic for parsing inputs, treating empty as 0.
     */
    private int parseInput(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            int val = Integer.parseInt(text.trim());
            return Math.max(0, val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
