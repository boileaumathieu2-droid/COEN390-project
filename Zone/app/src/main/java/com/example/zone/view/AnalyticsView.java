package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zone.R;

public class AnalyticsView extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem option) {
        if (option.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsView.class));
            return true;
        }
        return super.onOptionsItemSelected(option);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_page);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Analytics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Button mainMenuButton = findViewById(R.id.mainMenuButton);
        mainMenuButton.setOnClickListener(v -> finish());

        // Button for previous sessions is not implemented yet as per request
        Button previousSessionsButton = findViewById(R.id.previousSessionsButton);
    }
}
