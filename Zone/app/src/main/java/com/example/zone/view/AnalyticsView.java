package com.example.zone.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.HeartRateSensorManager;
import com.example.zone.model.HeartRateReading;
import com.example.zone.model.StudySessionModel;
import com.example.zone.model.TimerModel;
import com.example.zone.model.VirtualDatabase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsView extends AppCompatActivity {

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final TimerModel timer = TimerModel.getInstance();

    private TextView currentValue;
    private TextView restingValue;
    private TextView minValue;
    private TextView maxValue;
    private LineChart chart;
    private StudySessionModel latestSavedSession;
    private int lastPlottedCount = -1;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshAnalytics();
            refreshHandler.postDelayed(this, 1_000L);
        }
    };

        @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem option) {
        if (option.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsView.class));
            return true;
        }
        return super.onOptionsItemSelected(option);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_page);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        currentValue = findViewById(R.id.heartRateValue);
        restingValue = findViewById(R.id.restingHeartRateValue);
        minValue = findViewById(R.id.minHeartRateValue);
        maxValue = findViewById(R.id.maxHeartRateValue);
        chart = findViewById(R.id.heartRateChart);

        findViewById(R.id.mainMenuButton).setOnClickListener(view -> finish());
        findViewById(R.id.previousSessionsButton).setOnClickListener(view ->
                startActivity(new Intent(this, SessionHistoryView.class)));
        findViewById(R.id.recommendedStudyTimesButton).setOnClickListener(view ->
                startActivity(new Intent(this, RecommendedStudyTimesView.class)));

        configureChart();
        loadLatestSavedSession();
        refreshAnalytics();
    }

    private void loadLatestSavedSession() {
        new VirtualDatabase().getStudySessions(sessions -> {
            sessions.sort((left, right) -> {
                if (left.getStartTime() == null) return 1;
                if (right.getStartTime() == null) return -1;
                return right.getStartTime().compareTo(left.getStartTime());
            });
            latestSavedSession = sessions.isEmpty() ? null : sessions.get(0);
            lastPlottedCount = -1;
            refreshAnalytics();
        });
    }

    private void refreshAnalytics() {
        HeartRateReading stableReading = HeartRateSensorManager
                .getInstance(getApplicationContext())
                .getLastStableReading();
        int currentBpm = stableReading != null
                && stableReading.hasGoodSignal()
                ? stableReading.getBpm() : 0;
        currentValue.setText(valueOrDash(currentBpm));

        StudySessionModel displayedSession = timer.getLiveSession();
        if (displayedSession == null) {
            displayedSession = timer.getLastCompletedSession();
        }
        if (displayedSession == null) {
            displayedSession = latestSavedSession;
        }

        if (displayedSession == null) {
            restingValue.setText("--");
            minValue.setText("--");
            maxValue.setText("--");
            if (lastPlottedCount != 0) {
                plot(new int[0]);
            }
            return;
        }

        restingValue.setText(valueOrDash(displayedSession.getRestingHeartRate()));
        minValue.setText(valueOrDash(displayedSession.getMinHeartRate()));
        maxValue.setText(valueOrDash(displayedSession.getMaxHeartRate()));
        int[] data = displayedSession.getHeartRateData();
        if (data.length != lastPlottedCount) {
            plot(data);
        }
    }

    private String valueOrDash(int value) {
        return value > 0 ? String.valueOf(value) : "--";
    }

    private void configureChart() {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);
        chart.setNoDataText("Start a session to collect heart-rate data.");
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
    }

    private void plot(int[] heartRates) {
        lastPlottedCount = heartRates == null ? 0 : heartRates.length;
        if (heartRates == null || heartRates.length == 0) {
            chart.clear();
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        int minimum = heartRates[0];
        int maximum = heartRates[0];
        for (int i = 0; i < heartRates.length; i++) {
            entries.add(new Entry(i, heartRates[i]));
            minimum = Math.min(minimum, heartRates[i]);
            maximum = Math.max(maximum, heartRates[i]);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Heart Rate (BPM)");
        dataSet.setColor(Color.rgb(216, 50, 90));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(Color.rgb(216, 50, 90));
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        chart.setData(new LineData(dataSet));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(Math.max(30f, minimum - 10f));
        leftAxis.setAxisMaximum(Math.min(230f, Math.max(maximum + 10f, minimum + 20f)));
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHandler.removeCallbacks(refreshRunnable);
        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        refreshHandler.removeCallbacks(refreshRunnable);
        super.onPause();
    }
}
