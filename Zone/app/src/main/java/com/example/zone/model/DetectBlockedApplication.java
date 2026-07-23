package com.example.zone.model;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.model.StudySessionModel;
import com.example.zone.view.MainView;

public class DetectBlockedApplication extends AppCompatActivity {
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_application);
        button = findViewById(R.id.button);

        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainView.class);
            startActivity(intent);

        });
    }
}


