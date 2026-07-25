package com.example.zone.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.model.Database;
import com.example.zone.model.StudySessionModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsView extends AppCompatActivity {

    private final Handler refreshHandler =
            new Handler(Looper.getMainLooper());

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            updateLiveHeartRate();
            refreshHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_page);

        configureActionBar();
        configureButtons();
        setupAnalytics();
    }

    private void configureActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Analytics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void configureButtons() {
        Button mainMenuButton = findViewById(R.id.mainMenuButton);

        if (mainMenuButton != null) {
            mainMenuButton.setOnClickListener(view -> finish());
        }

        Button previousSessionsButton =
                findViewById(R.id.previousSessionsButton);

        if (previousSessionsButton != null) {
            previousSessionsButton.setOnClickListener(view -> {
                Intent intent = new Intent(
                        AnalyticsView.this,
                        SessionHistoryView.class
                );

                startActivity(intent);
            });
        }
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

    @Override
    protected void onDestroy() {
        refreshHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem option) {
        if (option.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(
                    this,
                    SettingsView.class
            );

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(option);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void updateLiveHeartRate() {
        TextView heartRateValue =
                findViewById(R.id.heartRateValue);

        if (heartRateValue == null) {
            return;
        }

        StudySessionModel liveSession =
                StudySessionModel.getInstance();

        if (liveSession == null) {
            heartRateValue.setText("N/A");
            return;
        }

        int currentHeartRate =
                liveSession.getHeartRateReading();

        if (currentHeartRate > 0) {
            heartRateValue.setText(
                    String.valueOf(currentHeartRate)
            );
        } else {
            heartRateValue.setText("N/A");
        }
    }

    private void setupAnalytics() {
        LineChart chart =
                findViewById(R.id.heartRateChart);

        if (chart == null) {
            return;
        }

        StudySessionModel liveSession =
                StudySessionModel.getInstance();

        int[] heartRateData;

        if (liveSession != null && liveSession.isActive()) {
            heartRateData =
                    liveSession.getHeartRateData();
        } else {
            heartRateData =
                    loadLastSessionHeartRateData();
        }

        updateLiveHeartRate();
        displayGraph(chart, heartRateData);
    }

    private int[] loadLastSessionHeartRateData() {
        String username = getSharedPreferences(
                "ZonePrefs",
                MODE_PRIVATE
        ).getString("username", null);

        if (username == null || username.trim().isEmpty()) {
            return new int[0];
        }

        try (Database database = new Database(this)) {
            int userID =
                    database.getUserID(username);

            return database.getLastSessionHeartRateData(
                    userID
            );
        }
    }

    private void displayGraph(
            LineChart chart,
            int[] heartRateData
    ) {
        if (heartRateData == null
                || heartRateData.length == 0) {

            chart.clear();
            chart.setNoDataText(
                    "No heart rate data available for this session."
            );

            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();

        for (int index = 0;
             index < heartRateData.length;
             index++) {

            entries.add(
                    new Entry(
                            index,
                            heartRateData[index]
                    )
            );
        }

        LineDataSet dataSet = new LineDataSet(
                entries,
                "Heart Rate (BPM)"
        );

        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setDrawValues(false);
        dataSet.setMode(
                LineDataSet.Mode.CUBIC_BEZIER
        );
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.RED);
        dataSet.setFillAlpha(50);

        LineData lineData =
                new LineData(dataSet);

        chart.setData(lineData);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(
                XAxis.XAxisPosition.BOTTOM
        );
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(40f);
        leftAxis.setAxisMaximum(200f);
        leftAxis.setDrawGridLines(true);

        chart.getAxisRight().setEnabled(false);

        chart.animateX(1000);
        chart.invalidate();
    }
}