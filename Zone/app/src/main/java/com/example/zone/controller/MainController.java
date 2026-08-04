package com.example.zone.controller;

import android.content.Context;
import android.content.Intent;
import com.example.zone.view.TimerSettingsView;

public class MainController {

    private final Context context;

    public MainController(Context context) {
        this.context = context;
    }

    public void onTimerSettingsClicked() {
        Intent intent = new Intent(context, TimerSettingsView.class);
        context.startActivity(intent);
    }
}
