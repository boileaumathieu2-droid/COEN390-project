package com.example.zone.view;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
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

    private Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analytics_page);

        Button mainMenuButton = findViewById(R.id.mainMenuButton);
        mainMenuButton.setOnClickListener(v -> finish());

        // Button for previous sessions
        Button previousSessionsButton = findViewById(R.id.previousSessionsButton);
        previousSessionsButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, SessionHistoryView.class);
            startActivity(intent);
        });

        setupAnalytics();
        
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                updateLiveHeartRate();
                refreshHandler.postDelayed(this, 1000); // Update every second
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void updateLiveHeartRate() {
        TextView heartRateValue = findViewById(R.id.heartRateValue);
        StudySessionModel liveSession = StudySessionModel.getInstance();
        
        if (liveSession != null) {
            int currentHR = liveSession.getHeartRateReading();
            heartRateValue.setText(currentHR > 0 ? String.valueOf(currentHR) : "N/A");
        }
    }

    private void setupAnalytics() {
        LineChart chart = findViewById(R.id.heartRateChart);

        StudySessionModel liveSession = StudySessionModel.getInstance();
        int[] data;

        if (liveSession != null && liveSession.isActive()) {
            data = liveSession.getHeartRateData();
        } else {
            // Fetch from database
            Database db = new Database(this);
            String username = getSharedPreferences("ZonePrefs", MODE_PRIVATE).getString("username", null);
            if (username != null) {
                int userID = db.getUserID(username);
                data = db.getLastSessionHeartRateData(userID);
            } else {
                data = new int[0];
            }
        }
        
        updateLiveHeartRate();
        displayGraph(chart, data);
    }

    private void displayGraph(LineChart chart, int[] heartRateData) {
        if (heartRateData == null || heartRateData.length == 0) {
            chart.setNoDataText("No heart rate data available for this session.");
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < heartRateData.length; i++) {
            entries.add(new Entry(i, heartRateData[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Heart Rate (BPM)");
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.RED);
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Chart styling
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
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
