package com.example.zone.view;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.ObjectiveController;
import com.example.zone.model.Database;
import com.example.zone.model.Objective;
import com.example.zone.model.ObjectiveAdapter;
import com.example.zone.model.Session;
import com.example.zone.model.VirtualDatabase;
import com.google.android.material.card.MaterialCardView;

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
    private MaterialCardView todayObjCard;
    private MaterialCardView futureObjCard;
    private TextView todayObjText;
    private TextView futureObjText;

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
        todayObjText = findViewById(R.id.todayObjectivesTextView);
        futureObjText = findViewById(R.id.futureObjectivesTextView);
        todayObjCard = findViewById(R.id.todayCard);
        futureObjCard = findViewById(R.id.futureCard);
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
        
        String status = objective.isCompleted() ? " (Completed)" : " (Pending)";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(objective.getEventName() + status)
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
                .setNegativeButton(R.string.cancel, null);

        if (objective.isCompleted()) {
            builder.setNegativeButton("Mark as Incomplete", (dialog, which) -> 
                    updateCompletion(objective, false));
        } else {
            builder.setPositiveButton("Mark as Completed", (dialog, which) -> 
                    updateCompletion(objective, true));
            // Restore edit button in a different way or just have it in the message?
            // AlertDialog only has 3 buttons.
        }
        
        // Actually, let's use a custom dialog or just add more buttons if possible.
        // Standard AlertDialog has limit of 3 buttons.
        
        // Let's refine this.
        new AlertDialog.Builder(this)
                .setTitle(objective.getEventName() + status)
                .setMessage(getString(
                        R.string.task_details,
                        objective.getTaskType(),
                        objective.getObjectiveDate(),
                        completion,
                        objective.getObjectiveText()))
                .setPositiveButton(objective.isCompleted() ? "Mark as Incomplete" : "Mark as Completed", 
                        (dialog, which) -> updateCompletion(objective, !objective.isCompleted()))
                .setNeutralButton(R.string.edit_task, (dialog, which) -> openTaskEditor(objective))
                .setNegativeButton(R.string.delete_task, (dialog, which) -> confirmDelete(objective))
                .show();
    }

    private void updateCompletion(Objective objective, boolean completed) {
        VirtualDatabase db = new VirtualDatabase();
        db.updateObjectiveCompletion(objective.getObjectiveID(), completed, success -> {
            if (success) {
                objective.setCompleted(completed);
                refreshObjectives();
            } else {
                android.widget.Toast.makeText(this, "Failed to update task", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
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
        VirtualDatabase db = new VirtualDatabase();
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_task_question)
                .setMessage(R.string.delete_task_message)
                .setPositiveButton(R.string.delete_task, (dialog, which) -> {
                    db.deleteTask(objective.getObjectiveID(), success -> {
                        if (success) {
                            todayObjectives.remove(objective);
                            futureObjectives.remove(objective);
                            todayAdapter.notifyDataSetChanged();
                            futureAdapter.notifyDataSetChanged();
                            refreshObjectives();
                        } else {
                            android.widget.Toast.makeText(
                                    this,
                                    "Task could not be deleted",
                                    android.widget.Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }


    private void refreshObjectives() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        VirtualDatabase db = new VirtualDatabase();
        db.GetDailyObjectives(Objectives -> {
            todayObjectives.clear();
            todayObjectives.addAll(Objectives);
            todayAdapter.notifyDataSetChanged();
            if (todayObjectives.isEmpty()) {
                todayObjText.setVisibility(View.VISIBLE);
                todayObjCard.setVisibility(View.GONE);
            } else {
                todayObjText.setVisibility(View.GONE);
                todayObjCard.setVisibility(View.VISIBLE);
            }
        }, today);

        db.GetFutureObjectives(Objectives -> {
            futureObjectives.clear();
            futureObjectives.addAll(Objectives);
            futureAdapter.notifyDataSetChanged();
            if (futureObjectives.isEmpty()) {
                futureObjText.setVisibility(View.VISIBLE);
                futureObjCard.setVisibility(View.GONE);
            } else {
                futureObjText.setVisibility(View.GONE);
                futureObjCard.setVisibility(View.VISIBLE);
            }
        }, today);
    }


//     db.GetDailyObjectives(objectives -> {
//        selectedObjectives.clear();
//        selectedObjectives.addAll(objectives);
//        objectiveLabels.clear();
//        for (Objective objective : selectedObjectives) {
//            objectiveLabels.add(objective.getEventName());
//        }
//        if (selectedDateTitle != null) {
//            selectedDateTitle.setText(getString(R.string.selected_date_tasks, date));
//        }
//    private void refreshObjectives() {
//        VirtualDatabase db = new VirtualDatabase();
//        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        todayObjectives.clear();
//        todayObjectives.addAll(controller.getObjectivesForDate(Session.getUserID(), today));
//        futureObjectives.clear();
//        futureObjectives.addAll(controller.getObjectivesForFuture(Session.getUserID(), today));
//        todayAdapter.notifyDataSetChanged();
//        futureAdapter.notifyDataSetChanged();
//        if (todayObjectives.isEmpty()){
//            todayObjText.setVisibility(View.VISIBLE);
//            todayObjCard.setVisibility(View.GONE);
//        }
//        else {
//            todayObjText.setVisibility(View.GONE);
//            todayObjCard.setVisibility(View.VISIBLE);
//        }
//        if (futureObjectives.isEmpty()){
//            futureObjText.setVisibility(View.VISIBLE);
//            futureObjCard.setVisibility(View.GONE);
//        }
//        else {
//            futureObjText.setVisibility(View.GONE);
//            futureObjCard.setVisibility(View.VISIBLE);
//        }
//
//    }
//}
}
