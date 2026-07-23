package com.example.zone.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.ObjectiveController;
import com.example.zone.model.Database;
import com.example.zone.model.Session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskCreationView extends AppCompatActivity {
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_EVENT_NAME = "event_name";
    public static final String EXTRA_DUE_DATE = "due_date";
    public static final String EXTRA_COMPLETION_TIME = "completion_time";
    public static final String EXTRA_TASK_TYPE = "task_type";
    public static final String EXTRA_OBJECTIVES = "objectives";

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final Calendar selectedCalendar = Calendar.getInstance();

    private ObjectiveController controller;
    private EditText eventNameInput;
    private EditText completionTimeInput;
    private EditText objectivesInput;
    private Spinner taskTypeSpinner;
    private Button dueDateButton;
    private int taskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_creation);

        taskId = getIntent().getIntExtra(EXTRA_TASK_ID, -1);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    taskId >= 0 ? R.string.edit_task_title : R.string.create_task);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        controller = new ObjectiveController(new Database(this));
        eventNameInput = findViewById(R.id.eventNameInput);
        completionTimeInput = findViewById(R.id.completionTimeInput);
        objectivesInput = findViewById(R.id.objectivesInput);
        taskTypeSpinner = findViewById(R.id.taskTypeSpinner);
        dueDateButton = findViewById(R.id.dueDateButton);
        Button saveButton = findViewById(R.id.saveTaskButton);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.task_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskTypeSpinner.setAdapter(typeAdapter);

        String initialDate = getIntent().getStringExtra(EXTRA_DUE_DATE);
        if (initialDate != null) {
            try {
                Date parsedDate = dateFormat.parse(initialDate);
                if (parsedDate != null) {
                    selectedCalendar.setTime(parsedDate);
                }
            } catch (ParseException ignored) {
                selectedCalendar.setTime(new Date());
            }
        }
        updateDueDateButton();

        if (taskId >= 0) {
            eventNameInput.setText(getIntent().getStringExtra(EXTRA_EVENT_NAME));
            completionTimeInput.setText(getIntent().getStringExtra(EXTRA_COMPLETION_TIME));
            objectivesInput.setText(getIntent().getStringExtra(EXTRA_OBJECTIVES));
            selectTaskType(getIntent().getStringExtra(EXTRA_TASK_TYPE));
        }

        dueDateButton.setOnClickListener(view -> showDatePicker());
        saveButton.setOnClickListener(view -> saveTask());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showDatePicker() {
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(year, month, dayOfMonth);
                    updateDueDateButton();
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDueDateButton() {
        dueDateButton.setText(dateFormat.format(selectedCalendar.getTime()));
    }

    private void selectTaskType(String taskType) {
        if (taskType == null) {
            return;
        }
        for (int index = 0; index < taskTypeSpinner.getCount(); index++) {
            if (taskType.equalsIgnoreCase(taskTypeSpinner.getItemAtPosition(index).toString())) {
                taskTypeSpinner.setSelection(index);
                return;
            }
        }
    }

    private void saveTask() {
        String eventName = eventNameInput.getText().toString().trim();
        String completionTime = completionTimeInput.getText().toString().trim();
        String objectives = objectivesInput.getText().toString().trim();
        String taskType = taskTypeSpinner.getSelectedItem().toString();
        String dueDate = dateFormat.format(selectedCalendar.getTime());

        if (eventName.isEmpty()) {
            eventNameInput.setError(getString(R.string.event_name_required));
            return;
        }
        if (completionTime.isEmpty()) {
            completionTimeInput.setError(getString(R.string.completion_time_required));
            return;
        }

        if (taskId >= 0) {
            controller.updateTask(
                    taskId, eventName, dueDate, completionTime, taskType, objectives);
        } else {
            controller.addTask(
                    Session.getUserID(), eventName, dueDate, completionTime, taskType, objectives);
        }
        Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
