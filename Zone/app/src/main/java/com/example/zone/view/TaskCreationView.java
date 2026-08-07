package com.example.zone.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.ObjectiveController;
import com.example.zone.model.Database;
import com.example.zone.model.Objective;
import com.example.zone.model.Session;
import com.example.zone.model.VirtualDatabase;
import com.example.zone.model.ObjectiveAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskCreationView extends AppCompatActivity {
    private static final int MAX_ESTIMATED_MINUTES = 1440;
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_EVENT_NAME = "event_name";
    public static final String EXTRA_DUE_DATE = "due_date";
    public static final String EXTRA_COMPLETION_TIME = "completion_time";
    public static final String EXTRA_TASK_TYPE = "task_type";
    public static final String EXTRA_OBJECTIVES = "objectives";

    private VirtualDatabase db= new VirtualDatabase();

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final Calendar selectedCalendar = Calendar.getInstance();

    private ObjectiveController controller;
    private EditText eventNameInput;
    private EditText completionTimeInput;
    private EditText objectivesInput;
    private Spinner taskTypeSpinner;
    private Button dueDateButton;
    private Button saveButton;
    private String taskId;
    private ListView completedList;
    private TextView completedTitle;
    private ArrayList<Objective> completedObjectives = new ArrayList<>();
    private com.example.zone.model.ObjectiveAdapter completedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_creation);

        taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    taskId != null ? R.string.edit_task_title : R.string.create_task);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        controller = new ObjectiveController(new Database(this));
        eventNameInput = findViewById(R.id.eventNameInput);
        completionTimeInput = findViewById(R.id.completionTimeInput);
        objectivesInput = findViewById(R.id.objectivesInput);
        taskTypeSpinner = findViewById(R.id.taskTypeSpinner);
        dueDateButton = findViewById(R.id.dueDateButton);
        saveButton = findViewById(R.id.saveTaskButton);
        completedList = findViewById(R.id.completedObjectivesList);
        completedTitle = findViewById(R.id.completedTitle);

        completedAdapter = new com.example.zone.model.ObjectiveAdapter(this, completedObjectives);
        completedList.setAdapter(completedAdapter);

        loadCompletedObjectives();

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

        if (taskId !=  null) {
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
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(year, month, dayOfMonth);
                    updateDueDateButton();
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        dialog.getDatePicker().setMinDate(today.getTimeInMillis());
        dialog.show();
    }

    private void updateDueDateButton() {
        dueDateButton.setText(dateFormat.format(selectedCalendar.getTime()));
    }

    private void loadCompletedObjectives() {
        String today = dateFormat.format(new Date());
        db.GetDailyObjectives(objectives -> {
            completedObjectives.clear();
            for (Objective obj : objectives) {
                if (obj.isCompleted()) {
                    completedObjectives.add(obj);
                }
            }
            if (!completedObjectives.isEmpty()) {
                completedTitle.setVisibility(View.VISIBLE);
                completedList.setVisibility(View.VISIBLE);
                completedAdapter.notifyDataSetChanged();
            }
        }, today);
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
        int estimatedMinutes;
        try {
            estimatedMinutes = Integer.parseInt(completionTime);
        } catch (NumberFormatException error) {
            completionTimeInput.setError(getString(R.string.completion_time_range));
            return;
        }
        if (estimatedMinutes < 1 || estimatedMinutes > MAX_ESTIMATED_MINUTES) {
            completionTimeInput.setError(getString(R.string.completion_time_range));
            return;
        }
        String normalizedCompletionTime = String.valueOf(estimatedMinutes);
        if (isBeforeToday(selectedCalendar)) {
            Toast.makeText(this, R.string.past_due_date_not_allowed, Toast.LENGTH_LONG).show();
            return;
        }

        saveButton.setEnabled(false);
        VirtualDatabase.AuthCallback callback = success -> {
            saveButton.setEnabled(true);
            if (!success) {
                Toast.makeText(this, "Task could not be saved", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        };

        if (taskId != null) {
            db.editTask(
                    taskId,
                    objectives,
                    dueDate,
                    eventName,
                    normalizedCompletionTime,
                    taskType,
                    callback
            );
        } else {
            db.saveObjective(
                    objectives,
                    dueDate,
                    eventName,
                    normalizedCompletionTime,
                    taskType,
                    callback
            );
        }
    }

    private boolean isBeforeToday(Calendar date) {
        Calendar dueDate = (Calendar) date.clone();
        dueDate.set(Calendar.HOUR_OF_DAY, 0);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);
        dueDate.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return dueDate.before(today);
    }
}
