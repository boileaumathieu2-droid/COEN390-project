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
import com.example.zone.controller.HeartRateSensorManager;
import com.example.zone.controller.MainController;
import com.example.zone.controller.NotificationController;
import com.example.zone.controller.ObjectiveController;
import com.example.zone.model.BlockedAppsStore;
import com.example.zone.model.Database;
import com.example.zone.model.MainViewObjectiveAdapter;
import com.example.zone.model.Objective;
import com.example.zone.model.Session;
import com.example.zone.model.StudySessionModel;
import com.example.zone.model.StudyTipsModel;
import com.example.zone.model.TimerModel;
import com.example.zone.model.TimerSettingsModel;
import com.example.zone.model.VirtualDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainView extends Fragment {
    private static final long TIMER_UI_DELAY_MS = 500L;
    private static final long TIP_DELAY_MS = 45_000L;

    private SharedPreferences prefs;
    private StudyTipsModel tipModel;
    private MainController mainController;
    private ObjectiveController objectiveController;
    private MainViewObjectiveAdapter objectiveAdapter;
    private ArrayList<Objective> dailyObjectives;
    private String today;
    private VirtualDatabase db = new VirtualDatabase();

    private final TimerModel timer = TimerModel.getInstance();
    private final Handler timerUiHandler = new Handler(Looper.getMainLooper());
    private final Handler tipHandler = new Handler(Looper.getMainLooper());


    private TextView timerDisplay;
    private TextView timerTitle;
    private TextView tipText;
    private TextView objectivesPrompt;
    private ListView dailyGoals;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Button completeButton;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        TimerSettingsModel timerSettingsModel = new TimerSettingsModel(requireContext());
        timer.setStudyDuration(timerSettingsModel.getStudyDuration());
        timer.setBreakDuration(timerSettingsModel.getBreakDuration());
        timer.setBreakEnabled(timerSettingsModel.isBreakEnabled());

        View view = inflater.inflate(R.layout.activity_main, container, false);
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        objectiveController = new ObjectiveController(new Database(requireContext()));
        mainController = new MainController(requireContext());
        bindViews(view);
        setupDailyObjectives();
        setupStudyTips();
        setupButtons(view);

        // Reconnect silently only when Bluetooth permission has already been granted.
        HeartRateSensorManager.getInstance(requireContext().getApplicationContext())
                .autoConnectToSavedBluno();

        handleIntent(getActivity() != null ? getActivity().getIntent() : null);
        updateTimerUi();

        return view;
    }

    private void bindViews(View view) {
        timerDisplay = view.findViewById(R.id.timerDisplay);
        timerTitle = view.findViewById(R.id.timerTitle);
        tipText = view.findViewById(R.id.studyTipTextView);
        objectivesPrompt = view.findViewById(R.id.goalPrompt);
        dailyGoals = view.findViewById(R.id.dailyGoalsListView);
        startButton = view.findViewById(R.id.startStudySeshButton);
        pauseButton = view.findViewById(R.id.pauseTimer);
        resetButton = view.findViewById(R.id.resetTimer);
        completeButton = view.findViewById(R.id.completeTimer);
    }
//    System.out.println("this function is getting called");
//    VirtualDatabase db = new VirtualDatabase();
//    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//        db.GetDailyObjectives(todayList -> {
//        System.out.println("Today's Objectives:");
//        for (Objective objective : todayList) {
//            System.out.println(objective.getEventName());
//        }
//        todayObjectives.clear();
//        todayObjectives.addAll(todayList);
//        to
    private void setupDailyObjectives() {
        dailyObjectives = new ArrayList<>();
        objectiveAdapter = new MainViewObjectiveAdapter(requireContext(), dailyObjectives);
        dailyGoals.setAdapter(objectiveAdapter);
        dailyGoals.setOnItemClickListener((parent, view, position, id) -> {
            if (getActivity() instanceof MainContainerActivity) {
                ((MainContainerActivity) getActivity()).switchToTab(0);
            }
        });
        objectivesPrompt.setOnClickListener(v -> {
            if (getActivity() instanceof MainContainerActivity) {
                ((MainContainerActivity) getActivity()).switchToTab(0);
            }
        });
    }
    private void setupStudyTips() {
        tipModel = new StudyTipsModel();
        if (tipText != null) {
            String tip = tipModel.randomTip();
            tipText.setText("Study Tip: " + tip);
            tipText.setOnClickListener(v -> startActivity(new Intent(requireContext(), StudyTipsView.class)));
        }
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.timerSettings).setOnClickListener(v -> mainController.onTimerSettingsClicked());

        // Swiping replaces these buttons
        view.findViewById(R.id.objectivesButton).setVisibility(View.GONE);
        view.findViewById(R.id.analyticsButton).setVisibility(View.GONE);
        view.findViewById(R.id.gradesTrackerButton).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), GradesTrackerView.class)));

        startButton.setOnClickListener(v -> startStudyOrBreak());
        pauseButton.setOnClickListener(v -> pauseOrResume());
        resetButton.setOnClickListener(v -> resetTimer());
        completeButton.setOnClickListener(v -> completeCurrentPeriod());
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        if (intent.getBooleanExtra("Countdown", false)) {
            startStudyOrBreak();
            intent.removeExtra("Countdown");
        }
        if (intent.getBooleanExtra("complete", false)) {
            int rating = intent.getIntExtra("rating", -1);
            boolean objective = intent.getBooleanExtra("objective", false);
            StudySessionModel session = StudySessionModel.getInstance();
            session.setObjectiveMet(objective);
            session.setProductivityRating(rating);
            db.saveStudySession();
            intent.removeExtra("complete");
        }
    }
    private void startStudyOrBreak() {
        if (!timer.isBreakTime()) {
            BlockedAppsStore.requestPermissionIfNeeded(requireActivity());
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
        Toast.makeText(requireContext(), "Timer reset", Toast.LENGTH_SHORT).show();
    }

    private void completeCurrentPeriod() {
        boolean completingBreak = timer.isBreakTime();
        timer.completeSession();
        manageDnD(false);
        updateTimerUi();

        if (completingBreak) {
            Toast.makeText(requireContext(), "Break finished", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Study session complete", Toast.LENGTH_SHORT).show();
            openReflectionIfPending();
        }
    }

    private void openReflectionIfPending() {
        if (reflectionScreenOpen || !timer.claimPendingReflection()) {
            return;
        }
        reflectionScreenOpen = true;
        manageDnD(false);
        startActivity(new Intent(requireContext(), reflectionView.class));
    }
    private void refreshDailyObjectives() {
        VirtualDatabase vdb = new VirtualDatabase();
        vdb.GetDailyObjectives(objectives -> {
            dailyObjectives.clear();
            dailyObjectives.addAll(objectives);
            objectiveAdapter.notifyDataSetChanged();
            boolean empty = dailyObjectives.isEmpty();
            dailyGoals.setVisibility(empty ? View.GONE : View.VISIBLE);
            objectivesPrompt.setVisibility(empty ? View.VISIBLE : View.GONE);
            if (empty) {
                objectivesPrompt.setText("You have not set any goals for today. Swipe left to set goals.");
            }
            resizeDailyGoalsList();
        }, today);
    }



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
        if (pauseButton != null) {
            pauseButton.setText(timer.isRunning() ? "Pause" : "Resume");
            int controlsVisibility = activeOrPaused ? View.VISIBLE : View.GONE;
            pauseButton.setVisibility(controlsVisibility);
            if (resetButton != null) resetButton.setVisibility(controlsVisibility);
            if (completeButton != null) completeButton.setVisibility(controlsVisibility);
        }
        if (startButton != null) {
            startButton.setVisibility(activeOrPaused ? View.GONE : View.VISIBLE);
            startButton.setText(timer.isBreakTime()
                ? "Start Break" : "Start Study Session");
        }
        
        if (minutes == 0 && seconds == 0 && timer.isRunning()) {
            // Timer just finished
            if (hasDndAccess()) manageDnD(false);
            NotificationController notificationHelper = new NotificationController(requireContext());
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationHelper.sendNotifications("STUDY APP", "Session Finished!");
            }
        }
    }

    private boolean hasDndAccess() {
        if (getContext() == null) return false;
        SharedPreferences preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        NotificationManager manager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        return preferences.getBoolean("Mute", false)
                && manager != null
                && manager.isNotificationPolicyAccessGranted();
    }

    private void manageDnD(boolean enable) {
        if (getContext() == null) return;
        NotificationManager manager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
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
    public void onResume() {
        super.onResume();
        reflectionScreenOpen = false;
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        refreshDailyObjectives();
        timerUiHandler.removeCallbacks(timerUiUpdater);
        timerUiHandler.post(timerUiUpdater);
        tipHandler.removeCallbacks(tipUpdater);
        tipHandler.post(tipUpdater);
        
        if (getActivity() != null) {
            handleIntent(getActivity().getIntent());
        }
    }

    @Override
    public void onPause() {
        timerUiHandler.removeCallbacks(timerUiUpdater);
        tipHandler.removeCallbacks(tipUpdater);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        timerUiHandler.removeCallbacksAndMessages(null);
        tipHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }


}
