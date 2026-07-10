package com.example.zone.controller;

import com.example.zone.view.TimerSettingsView;
import com.example.zone.view.MainView;

public class MainController {

    private final MainView mainView;

    public MainController(MainView mainView) {
        this.mainView = mainView;
    }

    public void onTimerSettingsClicked() {
        // TODO: decision logic could go here later
        // this is useless for now
        mainView.openTimerSettings();
    }
}