package com.example.zone.view;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.zone.R;
import com.example.zone.controller.MainController;
import com.example.zone.controller.NotificationController;
import com.example.zone.controller.ObjectiveController;
import com.example.zone.model.Database;
import com.example.zone.model.MainViewObjectiveAdapter;
import com.example.zone.model.Objective;
import com.example.zone.model.Session;
import com.example.zone.model.StudySessionModel;
import com.example.zone.model.StudyTipsModel;
import com.example.zone.model.TimerModel;
import com.example.zone.model.VirtualDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainView extends Fragment {
    private SharedPreferences prefs;
    private StudyTipsModel tipModel;
    private MainController mainController;
    private ObjectiveController objectiveController;
    private MainViewObjectiveAdapter adapter;
    private String today;
    VirtualDatabase db = new VirtualDatabase();

    private TextView timerDisplay;
    private TextView tipText;
    private Handler handler = new Handler(Looper.getMainLooper());
    private final int delay = 45000;
    private ListView dailyGoals;
    private TextView objectivesPrompt;
    private ArrayList<Objective> dailyGoalsArray;
    private Button pauseButton;
    private Button resetButton;
    private Button startButton;
    private Button completeButton;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private StudySessionModel StudySession = StudySessionModel.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        objectiveController = new ObjectiveController(new Database(requireContext()));
        mainController = new MainController(requireContext());

        // define buttons
        Button timerSettingsButton = view.findViewById(R.id.timerSettings);
        startButton = view.findViewById(R.id.startStudySeshButton);
        pauseButton = view.findViewById(R.id.pauseTimer);
        resetButton = view.findViewById(R.id.resetTimer);
        completeButton = view.findViewById(R.id.completeTimer);
        Button gradesButton = view.findViewById(R.id.gradesTrackerButton);
        Button analyticsButton = view.findViewById(R.id.analyticsButton);
        Button objectivesButton = view.findViewById(R.id.objectivesButton);
        timerDisplay = view.findViewById(R.id.timerDisplay);
        objectivesPrompt = view.findViewById(R.id.goalPrompt);
        tipText = view.findViewById(R.id.studyTipTextView);

        // Hide swiping-replaced buttons
        analyticsButton.setVisibility(View.GONE);
        objectivesButton.setVisibility(View.GONE);

        dailyGoals = view.findViewById(R.id.dailyGoalsListView);
        dailyGoalsArray = new ArrayList<>();
        adapter = new MainViewObjectiveAdapter(requireContext(), dailyGoalsArray);
        dailyGoals.setAdapter(adapter);

        tipModel = new StudyTipsModel();
        tipText.setText(tipModel.randomTip());
        handler.postDelayed(tipUpdater, delay);

        refresh();

        objectivesPrompt.setOnClickListener(v -> {
            if (getActivity() instanceof MainContainerActivity) {
                ((MainContainerActivity) getActivity()).switchToTab(0);
            }
        });

        tipText.setOnClickListener(v -> startActivity(new Intent(requireContext(), StudyTipsView.class)));

        gradesButton.setOnClickListener(v -> startActivity(new Intent(requireContext(), GradesTrackerView.class)));

        timerSettingsButton.setOnClickListener(v -> mainController.onTimerSettingsClicked());

        startButton.setOnClickListener(v -> {
            StudySession.startSession();
            startCountdown();
            TimerModel model = TimerModel.getInstance();
            if (hasDndAccess()) manageDnD(true);
            if (model.isBreakTime()) {
                StudySession.setStatus(StudySessionModel.Status.INACTIVE);
            } else {
                StudySession = model.getLiveSession();
            }
            showStatus();
        });

        pauseButton.setOnClickListener(v -> {
            if (TimerModel.getInstance().isRunning()) {
                if (hasDndAccess()) manageDnD(false);
                StudySession.setStatus(StudySessionModel.Status.INACTIVE);
                TimerModel.getInstance().pauseTimer();
            } else {
                resumeCountdown();
                StudySession.setStatus(StudySessionModel.Status.ACTIVE);
                if (hasDndAccess()) manageDnD(true);
            }
            if (TimerModel.getInstance().isBreakTime()) {
                StudySession.setStatus(StudySessionModel.Status.INACTIVE);
            }
            updateTimerUI(view);
        });

        resetButton.setOnClickListener(v -> {
            TimerModel.getInstance().stopAndReset();
            updateTimerUI(view);
            StudySession.setStatus(StudySessionModel.Status.INACTIVE);
            showStatus();
            if (hasDndAccess()) manageDnD(false);
        });

        completeButton.setOnClickListener(v -> {
            TimerModel model = TimerModel.getInstance();
            model.completeSession();
            StudySession.setStatus(StudySessionModel.Status.COMPLETE);
            showStatus();
            if (hasDndAccess()) manageDnD(false);
            String message = model.isBreakEnabled() ? (model.isBreakTime() ? "Study Finished! Time for a Break" : "Break Finished! Time to Study.") : "Study Finished!";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            updateTimerUI(view);
            startActivity(new Intent(requireContext(), reflectionView.class));
        });

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                TimerModel model = TimerModel.getInstance();
                if (model.isRunning()) {
                    boolean wasBreakTime = model.isBreakTime();
                    boolean stillRunning = model.tick();
                    updateTimerUI(view);
                    if (stillRunning) {
                        timerHandler.postDelayed(this, 1000);
                    } else {
                        if (!wasBreakTime) {
                            startActivity(new Intent(requireContext(), reflectionView.class));
                        } else {
                            Toast.makeText(requireContext(), "Break Finished! Time to Study.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        };

        return view;
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        if (intent.getBooleanExtra("Countdown", false)) {
            startButton.performClick();
            intent.removeExtra("Countdown");
        }
        if (intent.getBooleanExtra("complete", false)) {
            int rating = intent.getIntExtra("rating", -1);
            boolean objective = intent.getBooleanExtra("objective", false);
            StudySession.setObjectiveMet(objective);
            StudySession.setProductivityRating(rating);
            db.saveStudySession();
            intent.removeExtra("complete");
        }
    }

    private void refresh() {
        dailyGoalsArray.clear();
        dailyGoalsArray.addAll(objectiveController.getObjectivesForDate(Session.getUserID(), today));
        adapter.notifyDataSetChanged();
        if (dailyGoalsArray.isEmpty()) {
            dailyGoals.setVisibility(View.GONE);
            objectivesPrompt.setVisibility(View.VISIBLE);
            objectivesPrompt.setText("You have not set any goals for today. Swipe left to set goals.");
        } else {
            dailyGoals.setVisibility(View.VISIBLE);
            objectivesPrompt.setVisibility(View.GONE);
        }
    }

    protected void startCountdown() {
        TimerModel model = TimerModel.getInstance();
        if (!model.isRunning()) {
            model.startTimer();
            updateTimerUI(getView());
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private void resumeCountdown() {
        TimerModel model = TimerModel.getInstance();
        if (!model.isRunning()) {
            model.resumeTimer();
            updateTimerUI(getView());
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            handleIntent(getActivity().getIntent());
        }
        refresh();
        updateTimerUI(getView());
        if (TimerModel.getInstance().isRunning()) {
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.post(timerRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void updateTimerUI(View view) {
        if (view == null) return;
        TimerModel model = TimerModel.getInstance();
        int mins = model.getMinutes();
        int secs = model.getSeconds();
        if (timerDisplay != null) {
            timerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
        }

        int currentDuration = model.isBreakTime() ? model.getBreakDuration() : model.getStudyDuration();
        boolean isTimerActive = model.isRunning() || (model.getRemainingTime() < currentDuration && model.getRemainingTime() > 0);

        int visibility = isTimerActive ? View.VISIBLE : View.GONE;
        if (pauseButton != null) pauseButton.setVisibility(visibility);
        if (resetButton != null) resetButton.setVisibility(visibility);
        if (completeButton != null) completeButton.setVisibility(visibility);

        TextView timerTitle = view.findViewById(R.id.timerTitle);
        if (timerTitle != null) timerTitle.setText(model.isBreakTime() ? "Break Time" : "Time for Study");
        if (pauseButton != null) pauseButton.setText(model.isRunning() ? "Pause" : "Resume");

        if (mins == 0 && secs == 0) {
            if (hasDndAccess()) manageDnD(true);
            StudySession.setStatus(StudySessionModel.Status.INACTIVE);
            NotificationController notificationHelper = new NotificationController(requireContext());
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationHelper.sendNotifications("STUDY APP", "GET BACK TO WORK!");
            }
        }
        if (startButton != null) startButton.setVisibility(isTimerActive ? View.GONE : View.VISIBLE);
    }

    public void manageDnD(boolean enable) {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null || !notificationManager.isNotificationPolicyAccessGranted()) return;

        try {
            notificationManager.setInterruptionFilter(enable ? NotificationManager.INTERRUPTION_FILTER_NONE : NotificationManager.INTERRUPTION_FILTER_ALL);
        } catch (SecurityException exception) {
            Log.w("DND", "Could not change Do Not Disturb state.", exception);
        }
    }

    private boolean hasDndAccess() {
        prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean mute = prefs.getBoolean("Mute", false);
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager != null && notificationManager.isNotificationPolicyAccessGranted() && mute;
    }

    public void showStatus() {
        String msg = StudySession.getStatus().name();
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private Runnable tipUpdater = new Runnable() {
        @Override
        public void run() {
            if (tipText != null) {
                tipText.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                    tipText.setText(tipModel.randomTip());
                    tipText.animate().alpha(1f).setDuration(500);
                });
            }
            handler.postDelayed(this, delay);
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(tipUpdater);
    }
}
