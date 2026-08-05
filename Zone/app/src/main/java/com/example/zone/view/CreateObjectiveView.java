package com.example.zone.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.ObjectiveController;
import com.example.zone.model.Database;
import com.example.zone.model.Objective;
import com.example.zone.model.Session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CreateObjectiveView extends AppCompatActivity {

    private EditText objectiveEditText;
    private TextView completedObjectivesList;
    private TextView failedObjectivesList;
    private ObjectiveController controller;
    private String today;
    private int currentObjectiveId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_objective);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Daily Objectives");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        controller = new ObjectiveController(new Database(this));
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        objectiveEditText = findViewById(R.id.objectiveEditText);
        completedObjectivesList = findViewById(R.id.completedObjectivesList);
        failedObjectivesList = findViewById(R.id.failedObjectivesList);

        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnAccomplished = findViewById(R.id.btnAccomplished);
        Button btnNotAccomplished = findViewById(R.id.btnNotAccomplished);

        btnSave.setOnClickListener(v -> saveObjective());
        btnCancel.setOnClickListener(v -> finish());

        btnAccomplished.setOnClickListener(v -> {
            if (currentObjectiveId != -1) {
                controller.markObjectiveCompleted(currentObjectiveId);
                Toast.makeText(this, "Objective Accomplished!", Toast.LENGTH_SHORT).show();
                refreshLists();
                clearInput();
            } else {
                Toast.makeText(this, "Save an objective first", Toast.LENGTH_SHORT).show();
            }
        });

        btnNotAccomplished.setOnClickListener(v -> {
            if (currentObjectiveId != -1) {
                controller.markObjectiveFailed(currentObjectiveId);
                Toast.makeText(this, "Objective Not Accomplished", Toast.LENGTH_SHORT).show();
                refreshLists();
                clearInput();
            } else {
                Toast.makeText(this, "Save an objective first", Toast.LENGTH_SHORT).show();
            }
        });

        loadTodaysObjective();
        refreshLists();
    }

    private void saveObjective() {
        String text = objectiveEditText.getText().toString().trim();
        if (text.isEmpty()) {
            objectiveEditText.setError("Please enter an objective");
            return;
        }

        if (currentObjectiveId == -1) {
            currentObjectiveId = (int) controller.addObjective(Session.getUserID(), text, today);
        } else {
            controller.updateObjective(currentObjectiveId, text, today);
        }
        Toast.makeText(this, "Objective Saved", Toast.LENGTH_SHORT).show();
        refreshLists();
    }

    private void loadTodaysObjective() {
        ArrayList<Objective> objectives = controller.getObjectivesForDate(Session.getUserID(), today);
        if (!objectives.isEmpty()) {
            Objective obj = objectives.get(0);
            currentObjectiveId = obj.getObjectiveID();
            objectiveEditText.setText(obj.getObjectiveText());
        }
    }

    private void refreshLists() {
        // Refresh completed list
        ArrayList<Objective> completed = controller.getCompletedObjectivesForDate(Session.getUserID(), today);
        StringBuilder completedText = new StringBuilder();
        if (completed.isEmpty()) {
            completedText.append(getString(R.string.no_objectives_completed));
        } else {
            for (Objective obj : completed) {
                completedText.append("• ").append(obj.getObjectiveText()).append("\n");
            }
        }
        completedObjectivesList.setText(completedText.toString().trim());

        // Refresh failed list
        ArrayList<Objective> failed = controller.getFailedObjectivesForDate(Session.getUserID(), today);
        StringBuilder failedText = new StringBuilder();
        if (failed.isEmpty()) {
            failedText.append(getString(R.string.no_objectives_failed));
        } else {
            for (Objective obj : failed) {
                failedText.append("• ").append(obj.getObjectiveText()).append("\n");
            }
        }
        failedObjectivesList.setText(failedText.toString().trim());
    }

    private void clearInput() {
        objectiveEditText.setText("");
        currentObjectiveId = -1;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
