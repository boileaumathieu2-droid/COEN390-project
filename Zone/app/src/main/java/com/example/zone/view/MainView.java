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


    private TextView tipText;
    private TextView objectivesPrompt;
    private ListView dailyGoals;
    private Button launchTimerButton;

    private boolean reflectionScreenOpen;

    private final Runnable timerUiUpdater = new Runnable() {
        @Override
        public void run() {
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

        timer.initialize(requireContext());
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

        return view;
    }

    private void bindViews(View view) {
        tipText = view.findViewById(R.id.studyTipTextView);
        objectivesPrompt = view.findViewById(R.id.goalPrompt);
        dailyGoals = view.findViewById(R.id.dailyGoalsListView);
        launchTimerButton = view.findViewById(R.id.launchTimerButton);
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

        launchTimerButton.setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), TimerView.class)));
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        if (intent.getBooleanExtra("Countdown", false)) {
            // Forward to TimerView if needed, or just handle here if it's simpler.
            // For now, let's just clear it.
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
