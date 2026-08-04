package com.example.zone.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;

public class AboutHelpView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_help);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About / Help");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
