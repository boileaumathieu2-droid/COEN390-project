package com.example.zone.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zone.R;
import com.example.zone.controller.AnalyticsController;
import com.example.zone.controller.HeartRateSensorManager;
import com.example.zone.model.HeartRateReading;
import com.example.zone.model.StudySessionModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsView extends Fragment {

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private AnalyticsController controller;

    private TextView currentValue;
    private TextView restingValue;
    private TextView minValue;
    private TextView maxValue;
    private LineChart chart;
    private int lastPlottedCount = -1;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshAnalytics();
            refreshHandler.postDelayed(this, 1000L);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.analytics_page, container, false);

        controller = new AnalyticsController();

        currentValue = view.findViewById(R.id.heartRateValue);
        restingValue = view.findViewById(R.id.restingHeartRateValue);
        minValue = view.findViewById(R.id.minHeartRateValue);
        maxValue = view.findViewById(R.id.maxHeartRateValue);
        chart = view.findViewById(R.id.heartRateChart);

        Button mainMenuButton = view.findViewById(R.id.mainMenuButton);
        if (mainMenuButton != null) mainMenuButton.setVisibility(View.GONE);

        Button previousSessionsButton = view.findViewById(R.id.previousSessionsButton);
        if (previousSessionsButton != null) {
            previousSessionsButton.setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), SessionHistoryView.class)));
        }

        Button recommendedStudyTimesButton = view.findViewById(R.id.recommendedStudyTimesButton);
        if (recommendedStudyTimesButton != null) {
            recommendedStudyTimesButton.setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), RecommendedStudyTimesView.class)));
        }

        configureChart();
        refreshAnalytics();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void refreshAnalytics() {
        if (getContext() == null) return;
        
        HeartRateReading stableReading = HeartRateSensorManager
                .getInstance(getContext().getApplicationContext())
                .getLastStableReading();
        int currentBpm = (stableReading != null && stableReading.hasGoodSignal()) ? stableReading.getBpm() : 0;
        
        if (currentValue != null) currentValue.setText(valueOrDash(currentBpm));

        controller.getSessionData(session -> {
            if (session == null) {
                if (restingValue != null) restingValue.setText("--");
                if (minValue != null) minValue.setText("--");
                if (maxValue != null) maxValue.setText("--");
                if (lastPlottedCount != 0) {
                    lastPlottedCount = 0;
                    chart.clear();
                    chart.invalidate();
                }
                return;
            }

            if (restingValue != null) restingValue.setText(valueOrDash(session.getRestingHeartRate()));
            if (minValue != null) minValue.setText(valueOrDash(session.getMinHeartRate()));
            if (maxValue != null) maxValue.setText(valueOrDash(session.getMaxHeartRate()));

            int[] data = session.getHeartRateData();
            if (data.length != lastPlottedCount) {
                displayGraph(chart, data);
            }
        });
    }

    private String valueOrDash(int value) {
        return value > 0 ? String.valueOf(value) : "--";
    }

    private void configureChart() {
        if (chart == null) return;
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

    private void displayGraph(LineChart chart, int[] heartRateData) {
        lastPlottedCount = heartRateData == null ? 0 : heartRateData.length;
        if (chart == null) return;
        if (heartRateData == null || heartRateData.length == 0) {
            chart.clear();
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        int minimum = heartRateData[0];
        int maximum = heartRateData[0];
        for (int i = 0; i < heartRateData.length; i++) {
            entries.add(new Entry(i, heartRateData[i]));
            if (heartRateData[i] > 0) {
                minimum = (minimum == 0) ? heartRateData[i] : Math.min(minimum, heartRateData[i]);
                maximum = Math.max(maximum, heartRateData[i]);
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Heart Rate (BPM)");
        dataSet.setColor(Color.rgb(216, 50, 90));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(Color.rgb(216, 50, 90));
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.RED);
        dataSet.setFillAlpha(50);

        chart.setData(new LineData(dataSet));

        YAxis leftAxis = chart.getAxisLeft();
        float minVal = (minimum > 10) ? minimum - 10 : 0;
        leftAxis.setAxisMinimum(Math.max(0f, minVal));
        leftAxis.setAxisMaximum(Math.min(230f, Math.max(maximum + 10f, minimum + 20f)));
        
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
}
