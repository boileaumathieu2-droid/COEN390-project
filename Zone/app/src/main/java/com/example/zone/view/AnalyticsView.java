package com.example.zone.view;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zone.R;

public class AnalyticsView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_page);

        Button mainMenuButton = findViewById(R.id.mainMenuButton);
        mainMenuButton.setOnClickListener(v -> finish());

        // Button for previous sessions is not implemented yet as per request
        Button previousSessionsButton = findViewById(R.id.previousSessionsButton);
    }
}
