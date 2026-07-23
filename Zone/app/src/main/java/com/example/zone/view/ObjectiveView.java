package com.example.zone.view;

import android.os.Bundle;
import android.content.Intent;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.ObjectiveController;
import com.example.zone.model.Database;
import com.example.zone.model.Objective;
import com.example.zone.model.ObjectiveAdapter;
import com.example.zone.model.Session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ObjectivesPageView extends AppCompatActivity {
    private final ArrayList<Objective> todayObjectives = new ArrayList<>();
    private final ArrayList<Objective> futureObjectives = new ArrayList<>();
    private ObjectiveController controller;
    private ObjectiveAdapter todayAdapter;
    private ObjectiveAdapter futureAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.objective_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Objectives");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        controller = new ObjectiveController(new Database(this));
        ListView todayList = findViewById(R.id.todayObjectivesList);
        ListView futureList = findViewById(R.id.futureObjectivesList);
        todayAdapter = new ObjectiveAdapter(this, todayObjectives);
        futureAdapter = new ObjectiveAdapter(this, futureObjectives);
        todayList.setAdapter(todayAdapter);
        futureList.setAdapter(futureAdapter);
        todayList.setOnItemClickListener((parent, view, position, id) ->
                showTaskPopup(todayObjectives.get(position)));
        futureList.setOnItemClickListener((parent, view, position, id) ->
                showTaskPopup(futureObjectives.get(position)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshObjectives();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showTaskPopup(Objective objective) {
        String completion = objective.getCompletionTime().isEmpty()
                ? getString(R.string.not_set)
                : getString(R.string.minutes_value, objective.getCompletionTime());
        new AlertDialog.Builder(this)
                .setTitle(objective.getEventName())
                .setMessage(getString(
                        R.string.task_details,
                        objective.getTaskType(),
                        objective.getObjectiveDate(),
                        completion,
                        objective.getObjectiveText()))
                .setPositiveButton(R.string.edit_task, (dialog, which) ->
                        openTaskEditor(objective))
                .setNeutralButton(R.string.delete_task, (dialog, which) ->
                        confirmDelete(objective))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openTaskEditor(Objective objective) {
        Intent intent = new Intent(this, TaskCreationView.class);
        intent.putExtra(TaskCreationView.EXTRA_TASK_ID, objective.getObjectiveID());
        intent.putExtra(TaskCreationView.EXTRA_EVENT_NAME, objective.getEventName());
        intent.putExtra(TaskCreationView.EXTRA_DUE_DATE, objective.getObjectiveDate());
        intent.putExtra(TaskCreationView.EXTRA_COMPLETION_TIME, objective.getCompletionTime());
        intent.putExtra(TaskCreationView.EXTRA_TASK_TYPE, objective.getTaskType());
        intent.putExtra(TaskCreationView.EXTRA_OBJECTIVES, objective.getObjectiveText());
        startActivity(intent);
    }

    private void confirmDelete(Objective objective) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_task_question)
                .setMessage(R.string.delete_task_message)
                .setPositiveButton(R.string.delete_task, (dialog, which) -> {
                    controller.deleteObjective(objective.getObjectiveID());
                    refreshObjectives();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void refreshObjectives() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        todayObjectives.clear();
        todayObjectives.addAll(controller.getObjectivesForDate(Session.getUserID(), today));
        futureObjectives.clear();
        futureObjectives.addAll(controller.getObjectivesForFuture(Session.getUserID(), today));
        todayAdapter.notifyDataSetChanged();
        futureAdapter.notifyDataSetChanged();
    }
}
