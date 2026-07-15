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
        int studyMins = parseOrDefault(timerSettingsView.getStudyMinsText(), "smin");
        int studySecs = parseOrDefault(timerSettingsView.getStudySecsText(), "ssec");
        timerModel.setStudyDuration(studyMins * 60 + studySecs);

        int breakMins = parseOrDefault(timerSettingsView.getBreakMinsText(), "bmin");
        int breakSecs = parseOrDefault(timerSettingsView.getBreakSecsText(), "bsec");
        timerModel.setBreakDuration(breakMins * 60 + breakSecs);
        
        timerModel.setBreakEnabled(timerSettingsView.isBreakTimerEnabled());

        timerSettingsView.finish();
        timerModel.stopAndReset();
    }

    public void cancelSettings() {
        timerSettingsView.finish();
    }

    /**
     * Controller helper: Logic for parsing inputs with default values.
     */
    private int parseOrDefault(String text, String type) {
        if (text == null || text.trim().isEmpty()) {
            switch (type) {
                case "smin": return 25;
                case "ssec": return 0;
                case "bmin": return 5;
                case "bsec": return 0;
                default: return 0;
            }
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
