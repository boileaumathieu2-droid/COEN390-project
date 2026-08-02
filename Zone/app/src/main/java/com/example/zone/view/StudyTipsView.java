package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;

public class StudyTipsView extends AppCompatActivity {

    private TextView physicalTitle;
    private LinearLayout physicalContent;
    private TextView stressTitle;
    private LinearLayout stressContent;
    private TextView examTitle;
    private LinearLayout examContent;

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

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
        setContentView(R.layout.study_tips_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Studying and Wellness Tips");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        physicalTitle = findViewById(R.id.physicalWellnessTitle);
        physicalContent = findViewById(R.id.physicalWellnessContent);
        stressTitle = findViewById(R.id.stressManagementTitle);
        stressContent = findViewById(R.id.stressManagementContent);
        examTitle = findViewById(R.id.examPrepTitle);
        examContent = findViewById(R.id.examPrepContent);

        physicalTitle.setOnClickListener(v -> {
            if (physicalContent.getVisibility() == View.GONE) {
                physicalContent.setVisibility(View.VISIBLE);
            } else {
                physicalContent.setVisibility(View.GONE);
            }
        });

        stressTitle.setOnClickListener(v -> {
            if (stressContent.getVisibility() == View.GONE) {
                stressContent.setVisibility(View.VISIBLE);
            } else {
                stressContent.setVisibility(View.GONE);
            }
        });

        examTitle.setOnClickListener(v -> {
            if (examContent.getVisibility() == View.GONE) {
                examContent.setVisibility(View.VISIBLE);
            } else {
                examContent.setVisibility(View.GONE);
            }
        });


    }
}
