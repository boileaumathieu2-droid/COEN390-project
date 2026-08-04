package com.example.zone.view;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.zone.R;
import com.example.zone.controller.HeartRateSensorManager;
import com.example.zone.controller.NotificationController;
import com.example.zone.controller.ObjectiveController;
import com.example.zone.model.BlockedAppsStore;
import com.example.zone.model.Database;
import com.example.zone.model.MainViewObjectiveAdapter;
import com.example.zone.model.Objective;
import com.example.zone.model.Session;
import com.example.zone.model.StudyTipsModel;
import com.example.zone.model.TimerModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainView extends AppCompatActivity {

    private static final long TIMER_UI_DELAY_MS = 500L;
    private static final long TIP_DELAY_MS = 45_000L;

    private final TimerModel timer = TimerModel.getInstance();
    private final Handler timerUiHandler = new Handler(Looper.getMainLooper());
    private final Handler tipHandler = new Handler(Looper.getMainLooper());

    private ObjectiveController objectiveController;
    private MainViewObjectiveAdapter objectiveAdapter;
    private ArrayList<Objective> dailyObjectives;
    private String today;

    private TextView timerDisplay;
    private TextView timerTitle;
    private TextView tipText;
    private TextView objectivesPrompt;
    private ListView dailyGoals;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Button completeButton;
    private StudyTipsModel tipModel;
    private boolean reflectionScreenOpen;

    private final Runnable timerUiUpdater = new Runnable() {
        @Override
        public void run() {
            updateTimerUi();
            openReflectionIfPending();
            timerUiHandler.postDelayed(this, TIMER_UI_DELAY_MS);
        }
    };

    private final Runnable tipUpdater = new Runnable() {
        @Override
        public void run() {
            if (tipText != null && tipModel != null) {
                tipText.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction(() -> {
                            tipText.setText(tipModel.randomTip());
                            tipText.animate().alpha(1f).setDuration(500);
                        });
            }
            tipHandler.postDelayed(this, TIP_DELAY_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.init(getApplicationContext());
        setContentView(R.layout.activity_main);

        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        objectiveController = new ObjectiveController(new Database(this));

        bindViews();
        setupDailyObjectives();
        setupStudyTips();
        setupButtons();

        // Reconnect silently only when Bluetooth permission has already been granted.
        HeartRateSensorManager.getInstance(getApplicationContext())
                .autoConnectToSavedBluno();

        if (getIntent().getBooleanExtra("Countdown", false)) {
            startStudyOrBreak();
        }
        updateTimerUi();
    }

    private void bindViews() {
        timerDisplay = findViewById(R.id.timerDisplay);
        timerTitle = findViewById(R.id.timerTitle);
        tipText = findViewById(R.id.studyTipTextView);
        objectivesPrompt = findViewById(R.id.goalPrompt);
        dailyGoals = findViewById(R.id.dailyGoalsListView);
        startButton = findViewById(R.id.startStudySeshButton);
        pauseButton = findViewById(R.id.pauseTimer);
        resetButton = findViewById(R.id.resetTimer);
        completeButton = findViewById(R.id.completeTimer);
    }

    private void setupDailyObjectives() {
        dailyObjectives = new ArrayList<>();
        objectiveAdapter = new MainViewObjectiveAdapter(this, dailyObjectives);
        dailyGoals.setAdapter(objectiveAdapter);
        dailyGoals.setOnItemClickListener((parent, view, position, id) ->
                startActivity(new Intent(this, ObjectiveView.class)));
        objectivesPrompt.setOnClickListener(view ->
                startActivity(new Intent(this, ObjectiveView.class)));
    }

    private void setupStudyTips() {
        // This is the existing team feature. The timer/BLE fixes do not change it.
        tipModel = new StudyTipsModel();
        tipText.setText(tipModel.randomTip());
        tipText.setOnClickListener(view ->
                startActivity(new Intent(this, StudyTipsView.class)));
        tipHandler.postDelayed(tipUpdater, TIP_DELAY_MS);
    }

    private void setupButtons() {
        findViewById(R.id.timerSettings).setOnClickListener(view ->
                startActivity(new Intent(this, TimerSettingsView.class)));
        findViewById(R.id.objectivesButton).setOnClickListener(view ->
                startActivity(new Intent(this, ObjectiveView.class)));
        findViewById(R.id.gradesTrackerButton).setOnClickListener(view ->
                startActivity(new Intent(this, GradesTrackerView.class)));
        findViewById(R.id.analyticsButton).setOnClickListener(view ->
                startActivity(new Intent(this, AnalyticsView.class)));

        startButton.setOnClickListener(view -> startStudyOrBreak());
        pauseButton.setOnClickListener(view -> pauseOrResume());
        resetButton.setOnClickListener(view -> resetTimer());
        completeButton.setOnClickListener(view -> completeCurrentPeriod());
    }

    /** Kept public because MainController also routes to this screen. */
    public void openTimerSettings() {
        startActivity(new Intent(this, TimerSettingsView.class));
    }

    private void startStudyOrBreak() {
        if (!timer.isBreakTime()) {
            BlockedAppsStore.requestPermissionIfNeeded(this);
        }
        timer.startTimer();
        if (!timer.isBreakTime() && hasDndAccess()) {
            manageDnD(true);
        }
        updateTimerUi();
    }

    private void pauseOrResume() {
        if (timer.isRunning()) {
            timer.pauseTimer();
            manageDnD(false);
        } else {
            timer.resumeTimer();
            if (!timer.isBreakTime() && hasDndAccess()) {
                manageDnD(true);
            }
        }
        updateTimerUi();
    }

    private void resetTimer() {
        timer.stopAndReset();
        manageDnD(false);
        updateTimerUi();
        Toast.makeText(this, "Timer reset", Toast.LENGTH_SHORT).show();
    }

    private void completeCurrentPeriod() {
        boolean completingBreak = timer.isBreakTime();
        timer.completeSession();
        manageDnD(false);
        updateTimerUi();

        if (completingBreak) {
            Toast.makeText(this, "Break finished", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Study session complete", Toast.LENGTH_SHORT).show();
            openReflectionIfPending();
        }
    }

    private void openReflectionIfPending() {
        if (reflectionScreenOpen || !timer.claimPendingReflection()) {
            return;
        }
        reflectionScreenOpen = true;
        manageDnD(false);
        startActivity(new Intent(this, reflectionView.class));
    }

    private void refreshDailyObjectives() {
        if (dailyObjectives == null || objectiveController == null) {
            return;
        }
        dailyObjectives.clear();
        int userId = Session.getUserID();
        if (userId >= 0) {
            dailyObjectives.addAll(
                    objectiveController.getObjectivesForDate(userId, today)
            );
        }
        objectiveAdapter.notifyDataSetChanged();

        boolean empty = dailyObjectives.isEmpty();
        dailyGoals.setVisibility(empty ? View.GONE : View.VISIBLE);
        objectivesPrompt.setVisibility(empty ? View.VISIBLE : View.GONE);
        resizeDailyGoalsList();
    }

    /** The parent is already a ScrollView, so make every daily task visible. */
    private void resizeDailyGoalsList() {
        if (dailyObjectives.isEmpty()) {
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        int rowHeight = Math.round(72f * density);
        ViewGroup.LayoutParams params = dailyGoals.getLayoutParams();
        params.height = rowHeight * dailyObjectives.size()
                + dailyGoals.getDividerHeight() * Math.max(0, dailyObjectives.size() - 1);
        dailyGoals.setLayoutParams(params);
    }

    private void updateTimerUi() {
        int minutes = timer.getMinutes();
        int seconds = timer.getSeconds();
        timerDisplay.setText(String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        ));

        int fullDuration = timer.isBreakTime()
                ? timer.getBreakDuration() : timer.getStudyDuration();
        boolean activeOrPaused = timer.isRunning()
                || (timer.getRemainingTime() > 0
                && timer.getRemainingTime() < fullDuration);

        timerTitle.setText(timer.isBreakTime() ? "Break Time" : "Study Time");
        pauseButton.setText(timer.isRunning() ? "Pause" : "Resume");
        int controlsVisibility = activeOrPaused ? View.VISIBLE : View.GONE;
        pauseButton.setVisibility(controlsVisibility);
        resetButton.setVisibility(controlsVisibility);
        completeButton.setVisibility(controlsVisibility);
        startButton.setVisibility(activeOrPaused ? View.GONE : View.VISIBLE);
        startButton.setText(timer.isBreakTime()
                ? "Start Break" : "Start Study Session");
    }

    private boolean hasDndAccess() {
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        NotificationManager manager = getSystemService(NotificationManager.class);
        return preferences.getBoolean("Mute", false)
                && manager != null
                && manager.isNotificationPolicyAccessGranted();
    }

    private void manageDnD(boolean enable) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null || !manager.isNotificationPolicyAccessGranted()) {
            return;
        }
        try {
            manager.setInterruptionFilter(enable
                    ? NotificationManager.INTERRUPTION_FILTER_NONE
                    : NotificationManager.INTERRUPTION_FILTER_ALL);
        } catch (SecurityException exception) {
            Log.w("DND", "Could not change Do Not Disturb state", exception);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reflectionScreenOpen = false;
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        refreshDailyObjectives();
        timerUiHandler.removeCallbacks(timerUiUpdater);
        timerUiHandler.post(timerUiUpdater);
    }

    @Override
    protected void onPause() {
        timerUiHandler.removeCallbacks(timerUiUpdater);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        timerUiHandler.removeCallbacksAndMessages(null);
        tipHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsView.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
