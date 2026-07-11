package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;

public class SettingsView extends AppCompatActivity {
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.settings_menu);
        TextView logout = findViewById(R.id.logoutButton);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        logout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginView.class);
            startActivity(intent);
            Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show();
        });
        }
    }
