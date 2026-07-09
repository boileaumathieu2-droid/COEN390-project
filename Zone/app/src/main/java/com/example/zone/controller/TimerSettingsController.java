package com.example.zone.controller;

import android.widget.Button;

import com.example.zone.R;
import com.example.zone.view.TimerSettingsView;

public class TimerSettingsController {

    private TimerSettingsView timerSettingsView;

    public TimerSettingsController(TimerSettingsView activity) {
        this.timerSettingsView = activity;

        Button timerSettingsButton = activity.findViewById(R.id.timer_settings);
        timerSettingsButton.setOnClickListener(v -> breakTimerSwitch());
    }

    public void breakTimerSwitch() {
        // whatever this button is supposed to do

    }
}