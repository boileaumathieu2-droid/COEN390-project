package com.example.zone.view;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zone.R;
import com.example.zone.controller.MainController;
import com.example.zone.controller.NotificationController;
import com.example.zone.controller.ObjectiveController;
import com.example.zone.model.Database;
import com.example.zone.model.MainViewObjectiveAdapter;
import com.example.zone.model.Objective;
import com.example.zone.model.Session;
import com.example.zone.model.TimerModel;

import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.example.zone.model.StudySessionModel;
import com.example.zone.view.SettingsView;
import com.example.zone.view.TimerSettingsView;


public class MainView extends AppCompatActivity {

    private MainController mainController;
    private ObjectiveController objectiveController;
    private MainViewObjectiveAdapter adapter;
    private String today;

    private TextView timerDisplay;
    private ListView dailyGoals;
    private TextView objectivesPrompt;
    private ArrayList<Objective> dailyGoalsArray;
    private Button pauseButton;
    private Button resetButton;
    private Button startButton;
    private Button gradesButton;
    private Button completeButton;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private StudySessionModel StudySession = StudySessionModel.getInstance();

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
            Intent intent = new Intent(MainView.this, SettingsView.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(option);
    }

    private void refresh(){
        dailyGoalsArray.clear();

        dailyGoalsArray.addAll(objectiveController.getObjectivesForDate(Session.getUserID(), today));
        adapter.notifyDataSetChanged();
        if (dailyGoalsArray.isEmpty()) {
            dailyGoals.setVisibility(View.GONE);
            objectivesPrompt.setText("You have not set any goals for today. Set your study session goal here.");
        }
        else{
            dailyGoals.setVisibility(View.VISIBLE);
            objectivesPrompt.setText("New Study Goal");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.init(getApplicationContext());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        objectiveController = new ObjectiveController(new Database(this));
        mainController = new MainController(this);


        // define buttons
        Button timerSettingsButton = findViewById(R.id.timerSettings);
        startButton = findViewById(R.id.startStudySeshButton);
        pauseButton = findViewById(R.id.pauseTimer);
        resetButton = findViewById(R.id.resetTimer);
        gradesButton = findViewById(R.id.gradesTrackerButton);
        completeButton = findViewById(R.id.completeTimer);
        Button gradesButton = findViewById(R.id.gradesTrackerButton);
        Button analyticsButton = findViewById(R.id.analyticsButton);
        Button objectivesButton = findViewById(R.id.objectivesButton);
        timerDisplay = findViewById(R.id.timerDisplay);
        objectivesPrompt = findViewById(R.id.goalPrompt);

        dailyGoals = findViewById(R.id.dailyGoalsListView);
        dailyGoalsArray = new ArrayList<>();
        adapter = new MainViewObjectiveAdapter(this, dailyGoalsArray);

        dailyGoals.setAdapter(adapter);

        refresh();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        objectivesPrompt.setOnClickListener(v->{
            Intent intent = new Intent(this,ObjectiveView.class);
            startActivity(intent);
        });

        objectivesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ObjectiveView.class);
            startActivity(intent);
        });

        gradesButton.setOnClickListener(v->{
            Intent intent = new Intent(this, GradesTrackerView.class);
            startActivity(intent);
        });

        timerSettingsButton.setOnClickListener(v -> openTimerSettings()); // access to the openTimerSettings function

         startButton.setOnClickListener(v ->  {
            startCountdown();
            TimerModel model = TimerModel.getInstance();
            if (hasDndAccess()) {
                manageDnD(true);
            }
            if (model.isBreakTime()) {
                StudySession.setStatus(StudySessionModel.Status.INACTIVE);
            } else {
                StudySessionModel liveSession = model.getLiveSession();
                if (liveSession != null) {
                    StudySession = liveSession;
                }
            }
            showStatus();
        });

       pauseButton.setOnClickListener(v -> {
            // Checks if timer is counting down
            if (TimerModel.getInstance().isRunning()) {
                manageDnD(false);
                StudySession.setStatus(StudySessionModel.Status.INACTIVE);
                // pause timer if it was running
                TimerModel.getInstance().pauseTimer();

            } else {
                resumeCountdown();
                StudySession.setStatus(StudySessionModel.Status.ACTIVE);
                manageDnD(true);
            }
            if (TimerModel.getInstance().isBreakTime()) {
                StudySession.setStatus(StudySessionModel.Status.INACTIVE);
            }
            updateTimerUI();
        });

         resetButton.setOnClickListener(v -> {
            TimerModel.getInstance().stopAndReset();
            updateTimerUI();
            StudySession.setStatus(StudySessionModel.Status.INACTIVE);
            showStatus();
            manageDnD(false);
        });

        completeButton.setOnClickListener(v -> {
            TimerModel model = TimerModel.getInstance();
            model.completeSession();
            StudySession.setStatus(StudySessionModel.Status.COMPLETE);
            showStatus();
            manageDnD(false);
            // Show toast for manual completion
            String message;
            if(model.isBreakEnabled()) {
                message = model.isBreakTime() ? "Study Finished! Time for a Break" : "Break Finished! Time to Study.";
                StudySession.setStatus(StudySessionModel.Status.INACTIVE);
            } else {
                message = "Study Finished!";
            }
            StudySession.setStatus(StudySessionModel.Status.COMPLETE);

            Toast.makeText(MainView.this, message, Toast.LENGTH_SHORT).show();

            updateTimerUI();
        });

        gradesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GradesTrackerView.class);
            startActivity(intent);
        });

        analyticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnalyticsView.class);
            startActivity(intent);
        });

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                TimerModel model = TimerModel.getInstance();
                if (model.isRunning()) {
                    boolean stillRunning = model.tick();
                    updateTimerUI();
                    if (stillRunning) {
                        timerHandler.postDelayed(this, 1000);
                    } else {
                        String message;
                        if(model.isBreakEnabled()) {
                            message = model.isBreakTime() ? "Study Finished! Time for a Break" : "Break Finished! Time to Study.";
                        } else {
                            message = "Study Finished!";
                        }
                        Toast.makeText(MainView.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        updateTimerUI();
    }

    private void startCountdown() {
        TimerModel model = TimerModel.getInstance();
        if (!model.isRunning()) {
            // function inside of Timer Model
            model.startTimer(); // get the live session instance to upload data in
            updateTimerUI();
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private void resumeCountdown() {
        TimerModel model = TimerModel.getInstance();
        if (!model.isRunning()) {
            // resume countdown by keeping time instead
            model.resumeTimer();
            updateTimerUI();
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        updateTimerUI();
        // If the timer is already running (e.g. returning from settings), resume the UI updates
        if (TimerModel.getInstance().isRunning()) {
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.post(timerRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void updateTimerUI() {
        TimerModel model = TimerModel.getInstance();
        int minutes = model.getMinutes();
        int seconds = model.getSeconds();
        timerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)); // format the text

        // Logic for button visibility
        int currentDuration = model.isBreakTime() ? model.getBreakDuration() : model.getStudyDuration();    // change display depending on mode
        boolean isTimerActive = model.isRunning() || (model.getRemainingTime() < currentDuration && model.getRemainingTime() > 0);

        int visibility = isTimerActive ? android.view.View.VISIBLE : android.view.View.GONE;
        pauseButton.setVisibility(visibility);
        resetButton.setVisibility(visibility);
        completeButton.setVisibility(visibility);

        // Update title based on whether it is break time
        TextView timerTitle = findViewById(R.id.timerTitle);
        timerTitle.setText(model.isBreakTime() ? "Break Time" : "Time for Study");

        // state is true : false
        pauseButton.setText(model.isRunning() ? "Pause" : "Resume");
        if (minutes == 0 && seconds == 0) {
            manageDnD(false);
            StudySession.setStatus(StudySessionModel.Status.INACTIVE);
            NotificationController notificationHelper = new NotificationController(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationHelper.sendNotifications("STUDY APP", "GET BACK TO WORK!");
            }
        }

        // Hide start button if timer is running or paused mid-session
        startButton.setVisibility(isTimerActive ? android.view.View.GONE : android.view.View.VISIBLE);
    }

    public void openTimerSettings() {
        Intent intent = new Intent(this, TimerSettingsView.class);
        startActivity(intent);
    }
    public void manageDnD(boolean enable) {
        NotificationManager notificationManager =
                getSystemService(NotificationManager.class);
        if (notificationManager == null
                || !notificationManager.isNotificationPolicyAccessGranted()) {
            Log.d("DND", "Notification policy access is not granted; timer continues normally.");
            return;
        }

        try {
            notificationManager.setInterruptionFilter(enable
                    ? NotificationManager.INTERRUPTION_FILTER_NONE
                    : NotificationManager.INTERRUPTION_FILTER_ALL);
            Log.d("DND", "Current filter: "
                    + notificationManager.getCurrentInterruptionFilter());
        } catch (SecurityException exception) {
            Log.w("DND", "Could not change Do Not Disturb state.", exception);
        }
    }

    private boolean hasDndAccess() {
        NotificationManager notificationManager =
                getSystemService(NotificationManager.class);
        return notificationManager != null
                && notificationManager.isNotificationPolicyAccessGranted();
    }
    public void showStatus() {
        if (StudySession.getStatus() == StudySessionModel.Status.COMPLETE) {
            Toast.makeText(MainView.this, "COMPLETE", Toast.LENGTH_SHORT).show();
        } else if (StudySession.getStatus() == StudySessionModel.Status.INACTIVE) {
            Toast.makeText(MainView.this, "INACTIVE", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainView.this, "ACTIVE", Toast.LENGTH_SHORT).show();
        }
    }


}
