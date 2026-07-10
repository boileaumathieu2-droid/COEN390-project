package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zone.R;
import com.example.zone.controller.MainController;

public class MainView extends AppCompatActivity {

    private MainController mainController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mainController = new MainController(this);

        Button timerSettingsButton = findViewById(R.id.timerSettings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        timerSettingsButton.setOnClickListener(v -> openTimerSettings()); // access to the openTimerSettings function
    }

    public void openTimerSettings() {
        Intent intent = new Intent(this, TimerSettingsView.class);
        startActivity(intent);
    }


}
