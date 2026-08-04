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
    private Runnable refreshRunnable;
    private AnalyticsController controller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.analytics_page, container, false);

        controller = new AnalyticsController();

        Button mainMenuButton = view.findViewById(R.id.mainMenuButton);
        if (mainMenuButton != null) mainMenuButton.setVisibility(View.GONE);

        Button previousSessionsButton = view.findViewById(R.id.previousSessionsButton);
        if (previousSessionsButton != null) {
            previousSessionsButton.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), SessionHistoryView.class));
            });
        }

        Button recommendedStudyTimesButton = view.findViewById(R.id.recommendedStudyTimesButton);
        if (recommendedStudyTimesButton != null) {
            recommendedStudyTimesButton.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), RecommendedStudyTimesView.class));
            });
        }

        setupAnalytics(view);
        
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                updateLiveHeartRate(view);
                refreshHandler.postDelayed(this, 1000);
            }
        };
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

    private void updateLiveHeartRate(View view) {
        if (view == null) return;
        TextView heartRateValue = view.findViewById(R.id.heartRateValue);
        if (heartRateValue != null) {
            int currentHR = controller.getCurrentHeartRate();
            heartRateValue.setText(currentHR > 0 ? String.valueOf(currentHR) : "N/A");
        }
    }

    private void setupAnalytics(View view) {
        LineChart chart = view.findViewById(R.id.heartRateChart);
        if (chart == null) return;

        controller.getHeartRateData(data -> displayGraph(chart, data));
        updateLiveHeartRate(view);
    }

    private void displayGraph(LineChart chart, int[] heartRateData) {
        if (chart == null) return;
        if (heartRateData == null || heartRateData.length == 0) {
            chart.clear();
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
