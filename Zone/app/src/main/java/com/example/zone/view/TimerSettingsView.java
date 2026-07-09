package com.example.zone.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.widget.Button;
import com.example.zone.controller.MainController;
import android.content.Intent;
import androidx.core.view.WindowInsetsCompat;
import com.example.zone.controller.TimerSettingsController;

import com.example.zone.R;

public class TimerSettingsView extends AppCompatActivity{


    private TimerSettingsController timerSettingsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.timer_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.timer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        timerSettingsController = new TimerSettingsController(this);

        Button timerSettingsButton = findViewById(R.id.switch_break_timer);
        timerSettingsButton.setOnClickListener(v -> timerSettingsController.breakTimerSwitch());
    }


}
