package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.zone.R;
import com.example.zone.controller.ObjectiveController;
import com.example.zone.model.Database;
import com.example.zone.model.Objective;
import com.example.zone.model.Session;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ObjectiveView extends Fragment {
    private final ArrayList<Objective> selectedObjectives = new ArrayList<>();
    private final ArrayList<String> objectiveLabels = new ArrayList<>();
    private String date;
    private ObjectiveController controller;
    private ArrayAdapter<String> objectiveAdapter;
    private TextView selectedDateTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.objective_view, container, false);

        controller = new ObjectiveController(new Database(requireContext()));
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        CalendarView calendar = view.findViewById(R.id.objectiveCalendar);
        Button objectiveButton = view.findViewById(R.id.newObjectiveButton);
        Button myObjectives = view.findViewById(R.id.myObjectivesButton);
        ListView selectedDateTasks = view.findViewById(R.id.selectedDateTasks);
        TextView noTasksText = view.findViewById(R.id.noTasksText);
        selectedDateTitle = view.findViewById(R.id.selectedDateTitle);

        objectiveAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, objectiveLabels);
        selectedDateTasks.setAdapter(objectiveAdapter);
        selectedDateTasks.setEmptyView(noTasksText);
        selectedDateTasks.setOnItemClickListener((parent, v, position, id) ->
                showTaskPopup(selectedObjectives.get(position)));

        myObjectives.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ObjectivesPageView.class)));
        objectiveButton.setOnClickListener(v -> openTaskCreation(null));
        calendar.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            refreshSelectedDateTasks();
        });

        refreshSelectedDateTasks();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (controller != null) {
            refreshSelectedDateTasks();
        }
    }

    private void showTaskPopup(Objective objective) {
        String completion = objective.getCompletionTime().isEmpty()
                ? getString(R.string.not_set)
                : getString(R.string.minutes_value, objective.getCompletionTime());
        new AlertDialog.Builder(requireContext())
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
        Intent intent = new Intent(requireContext(), TaskCreationView.class);
        intent.putExtra(TaskCreationView.EXTRA_DUE_DATE,
                objective == null ? date : objective.getObjectiveDate());
        if (objective != null) {
            intent.putExtra(TaskCreationView.EXTRA_TASK_ID, objective.getObjectiveID());
            intent.putExtra(TaskCreationView.EXTRA_EVENT_NAME, objective.getEventName());
            intent.putExtra(TaskCreationView.EXTRA_COMPLETION_TIME, objective.getCompletionTime());
            intent.putExtra(TaskCreationView.EXTRA_TASK_TYPE, objective.getTaskType());
            intent.putExtra(TaskCreationView.EXTRA_OBJECTIVES, objective.getObjectiveText());
        }
        startActivity(intent);
    }

    private void confirmDelete(Objective objective) {
        new AlertDialog.Builder(requireContext())
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
        if (selectedDateTitle != null) {
            selectedDateTitle.setText(getString(R.string.selected_date_tasks, date));
        }
        objectiveAdapter.notifyDataSetChanged();
    }
}
