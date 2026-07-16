package com.example.zone.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zone.R;
import com.example.zone.controller.MainController;
import com.example.zone.model.TimerModel;

import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class MainView extends AppCompatActivity {

    private MainController mainController;
    private TextView timerDisplay;
    private Button pauseButton;
    private Button resetButton;
    private Button startButton;
    private Button gradesButton;
    private Button completeButton;
    private ScrollView objectiveWindow;
    private TextView objectiveText;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mainController = new MainController(this);

        // define buttons
        Button timerSettingsButton = findViewById(R.id.timerSettings);
        startButton = findViewById(R.id.startStudySeshButton);
        pauseButton = findViewById(R.id.pauseTimer);
        resetButton = findViewById(R.id.resetTimer);
        gradesButton = findViewById(R.id.gradesTrackerButton);
        completeButton = findViewById(R.id.completeTimer);
        timerDisplay = findViewById(R.id.timerDisplay);
        objectiveWindow = findViewById(R.id.dailyObjectiveWindow);
        objectiveText = findViewById(R.id.objectiveScrollableText);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        objectiveText.setOnClickListener( v-> {
            Intent intent = new Intent(this, ObjectiveView.class);
            startActivity(intent);
        });

        gradesButton.setOnClickListener(v->{
            Intent intent = new Intent(this, GradesTrackerView.class);
            startActivity(intent);
        });

        timerSettingsButton.setOnClickListener(v -> openTimerSettings()); // access to the openTimerSettings function

        startButton.setOnClickListener(v -> startCountdown());

        pauseButton.setOnClickListener(v -> {
            // Checks if timer is counting down
            if (TimerModel.getInstance().isRunning()) {
                // pause timer if it was running
                TimerModel.getInstance().pauseTimer();
            } else {
                // resume timer if it was paused
                resumeCountdown();
            }
            updateTimerUI();
        });

        resetButton.setOnClickListener(v -> {
            TimerModel.getInstance().stopAndReset();
            updateTimerUI();
        });

        completeButton.setOnClickListener(v -> {
            TimerModel model = TimerModel.getInstance();
            model.completeSession();

            // Show toast for manual completion
            String message;
            if(model.isBreakEnabled()) {
                message = model.isBreakTime() ? "Study Finished! Time for a Break" : "Break Finished! Time to Study.";
            } else {
                message = "Study Finished!";
            }
            Toast.makeText(MainView.this, message, Toast.LENGTH_SHORT).show();

            updateTimerUI();
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
            model.startTimer();
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

        // Hide start button if timer is running or paused mid-session
        startButton.setVisibility(isTimerActive ? android.view.View.GONE : android.view.View.VISIBLE);
    }

    public void openTimerSettings() {
        Intent intent = new Intent(this, TimerSettingsView.class);
        startActivity(intent);
    }


}

