package com.example.zone.view;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.model.BlockedAppsStore;
import com.example.zone.model.StudyTipsModel;
import com.example.zone.model.TimerModel;
import com.example.zone.model.TimerSettingsModel;
import com.example.zone.model.VirtualDatabase;

import java.util.Locale;

public class TimerView extends AppCompatActivity {
    private static final long TIMER_UI_DELAY_MS = 500L;
    private static final long TIP_DELAY_MS = 45_000L;

    private final TimerModel timer = TimerModel.getInstance();
    private final Handler timerUiHandler = new Handler(Looper.getMainLooper());
    private final Handler tipHandler = new Handler(Looper.getMainLooper());
    private StudyTipsModel tipModel;

    private TextView timerDisplay;
    private TextView timerTitle;
    private TextView tipText;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Button completeButton;
    private LinearLayout controlsLayout;

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
                            String tip = tipModel.randomTip();
                            tipText.setText("Study Tip: " + tip);
                            tipText.animate().alpha(1f).setDuration(500);
                        });
            }
            tipHandler.postDelayed(this, TIP_DELAY_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Timer");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        timer.initialize(this);
        TimerSettingsModel timerSettingsModel = new TimerSettingsModel(this);
        timer.setStudyDuration(timerSettingsModel.getStudyDuration());
        timer.setBreakDuration(timerSettingsModel.getBreakDuration());
        timer.setBreakEnabled(timerSettingsModel.isBreakEnabled());

        bindViews();
        setupStudyTips();
        setupButtons();

        updateTimerUi();
    }

    private void bindViews() {
        timerDisplay = findViewById(R.id.timerDisplay);
        timerTitle = findViewById(R.id.timerTitle);
        tipText = findViewById(R.id.studyTipTextView);
        startButton = findViewById(R.id.startStudySeshButton);
        pauseButton = findViewById(R.id.pauseTimer);
        resetButton = findViewById(R.id.resetTimer);
        completeButton = findViewById(R.id.completeTimer);
        controlsLayout = findViewById(R.id.timerControls);
    }

    private void setupStudyTips() {
        tipModel = new StudyTipsModel();
        if (tipText != null) {
            String tip = tipModel.randomTip();
            tipText.setText("Study Tip: " + tip);
        }
    }

    private void setupButtons() {
        startButton.setOnClickListener(v -> startStudyOrBreak());
        pauseButton.setOnClickListener(v -> pauseOrResume());
        resetButton.setOnClickListener(v -> resetTimer());
        completeButton.setOnClickListener(v -> completeCurrentPeriod());
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

    private void updateTimerUi() {
        if (timerDisplay == null) return;
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

        if (timerTitle != null) timerTitle.setText(timer.isBreakTime() ? "Break Time" : "Study Time");
        
        if (controlsLayout != null) {
            controlsLayout.setVisibility(activeOrPaused ? View.VISIBLE : View.GONE);
        }
        
        if (pauseButton != null) {
            pauseButton.setText(timer.isRunning() ? "Pause" : "Resume");
        }
        
        if (completeButton != null) {
            completeButton.setVisibility(activeOrPaused ? View.VISIBLE : View.GONE);
        }
        
        if (startButton != null) {
            startButton.setVisibility(activeOrPaused ? View.GONE : View.VISIBLE);
            startButton.setText(timer.isBreakTime()
                    ? "Start Break" : "Start Study Session");
        }
    }

    private boolean hasDndAccess() {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return preferences.getBoolean("Mute", false)
                && manager != null
                && manager.isNotificationPolicyAccessGranted();
    }

    private void manageDnD(boolean enable) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        reflectionScreenOpen = false;
        timerUiHandler.post(timerUiUpdater);
        tipHandler.post(tipUpdater);
    }

    @Override
    protected void onPause() {
        timerUiHandler.removeCallbacks(timerUiUpdater);
        tipHandler.removeCallbacks(tipUpdater);
        super.onPause();
    }
}
