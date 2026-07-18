package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ObjectiveView extends AppCompatActivity {

    private CalendarView calendar;
    private Button objectiveButton;
    private String date;
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;}
    public boolean onOptionsItemSelected(MenuItem option) {
        int id = option.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(ObjectiveView.this, SettingsView.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(option);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.objective_view);
        calendar = findViewById(R.id.objectiveCalendar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Set Objectives");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        objectiveButton = findViewById(R.id.newObjectiveButton);

        objectiveButton.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(ObjectiveView.this);

            View popupView = getLayoutInflater().inflate(R.layout.create_objective, null);
            builder.setView(popupView);

            AlertDialog dialog = builder.create();

            Button cancel = popupView.findViewById(R.id.btnCancel);
            Button save = popupView.findViewById(R.id.btnSave);
            EditText objectiveEdit = popupView.findViewById(R.id.objectiveEditText);

            cancel.setOnClickListener(x -> dialog.dismiss());

            save.setOnClickListener(x -> {
                String objective = objectiveEdit.getText().toString();
                dialog.dismiss();
            });

            dialog.show();
        });
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {

            date = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    dayOfMonth
            );

        });

    }

    }
