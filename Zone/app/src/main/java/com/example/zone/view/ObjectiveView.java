package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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

public class ObjectiveView extends AppCompatActivity {
    private final ArrayList<Objective> selectedObjectives = new ArrayList<>();
    private final ArrayList<String> objectiveLabels = new ArrayList<>();
    private String date;
    private ObjectiveController controller;
    private ArrayAdapter<String> objectiveAdapter;
    private TextView selectedDateTitle;

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
        setContentView(R.layout.objective_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Set Objectives");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        controller = new ObjectiveController(new Database(this));
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        CalendarView calendar = findViewById(R.id.objectiveCalendar);
        Button objectiveButton = findViewById(R.id.newObjectiveButton);
        Button myObjectives = findViewById(R.id.myObjectivesButton);
        ListView selectedDateTasks = findViewById(R.id.selectedDateTasks);
        TextView noTasksText = findViewById(R.id.noTasksText);
        selectedDateTitle = findViewById(R.id.selectedDateTitle);

        objectiveAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, objectiveLabels);
        selectedDateTasks.setAdapter(objectiveAdapter);
        selectedDateTasks.setEmptyView(noTasksText);
        selectedDateTasks.setOnItemClickListener((parent, view, position, id) ->
                showTaskPopup(selectedObjectives.get(position)));

        myObjectives.setOnClickListener(view ->
                startActivity(new Intent(this, ObjectivesPageView.class)));
        objectiveButton.setOnClickListener(view -> openTaskCreation(null));
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            date = String.format(
                    Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            refreshSelectedDateTasks();
        });

        refreshSelectedDateTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (controller != null) {
            refreshSelectedDateTasks();
        }
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
                        openTaskCreation(objective))
                .setNeutralButton(R.string.delete_task, (dialog, which) ->
                        confirmDelete(objective))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openTaskCreation(Objective objective) {
        Intent intent = new Intent(this, TaskCreationView.class);
        intent.putExtra(TaskCreationView.EXTRA_DUE_DATE,
                objective == null ? date : objective.getObjectiveDate());
        if (objective != null) {
            intent.putExtra(TaskCreationView.EXTRA_TASK_ID, objective.getObjectiveID());
            intent.putExtra(TaskCreationView.EXTRA_EVENT_NAME, objective.getEventName());
            intent.putExtra(
                    TaskCreationView.EXTRA_COMPLETION_TIME, objective.getCompletionTime());
            intent.putExtra(TaskCreationView.EXTRA_TASK_TYPE, objective.getTaskType());
            intent.putExtra(TaskCreationView.EXTRA_OBJECTIVES, objective.getObjectiveText());
        }
        startActivity(intent);
    }

    private void confirmDelete(Objective objective) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_task_question)
                .setMessage(R.string.delete_task_message)
                .setPositiveButton(R.string.delete_task, (dialog, which) -> {
                    controller.deleteObjective(objective.getObjectiveID());
                    refreshSelectedDateTasks();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void refreshSelectedDateTasks() {
        selectedObjectives.clear();
        selectedObjectives.addAll(controller.getObjectivesForDate(Session.getUserID(), date));
        objectiveLabels.clear();
        for (Objective objective : selectedObjectives) {
            objectiveLabels.add(objective.getEventName());
        }
        selectedDateTitle.setText(getString(R.string.selected_date_tasks, date));
        objectiveAdapter.notifyDataSetChanged();
    }
}
