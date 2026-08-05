package com.example.zone.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.RecommendedStudyTimesController;
import com.example.zone.model.Session;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class RecommendedStudyTimesView extends AppCompatActivity {

    private RecommendedStudyTimesController controller;
    private BarChart barChart;
    private TextView chartStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommended_study_times);

        barChart = findViewById(R.id.performanceBarChart);
        chartStatusText = findViewById(R.id.chartStatusText);
        Button backButton = findViewById(R.id.backToAnalyticsButton);
        Button mainMenuButton = findViewById(R.id.mainMenuButton);

        backButton.setOnClickListener(v -> finish());
        mainMenuButton.setOnClickListener(v -> finish());

        // Initialize session
        Session.init(getApplicationContext());

        controller = new RecommendedStudyTimesController(this);
        setupChart();
        displayData();
    }

    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.getLegend().setEnabled(false); // Remove legend

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int binIndex = (int) e.getX();
                showDetailPopup(binIndex);
            }

            @Override
            public void onNothingSelected() {}
        });

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(6);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(5.5f);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] timeRanges = {
                    "0h-3h", "4h-7h", "8h-11h", "12h-15h", "16h-19h", "20h-23h"
            };

            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < timeRanges.length) {
                    return timeRanges[index];
                }
                return "";
            }
        });

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(10f); // Rating scale 0-10
        leftAxis.setLabelCount(11, true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(Color.DKGRAY);

        barChart.getAxisRight().setEnabled(false);
    }

    private void displayData() {
        chartStatusText.setText(R.string.loading_recommendations);
        chartStatusText.setVisibility(View.VISIBLE);
        try {
            controller.getHourlyAverages(averages -> showAverages(averages));
        } catch (RuntimeException error) {
            showLoadError();
        }
    }

    private void showAverages(float[] averages) {
        runOnUiThread(() -> {
            List<BarEntry> entries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();

            for (int i = 0; i < 6; i++) {
                float val = averages[i];

                if (val >= 0) {
                    entries.add(new BarEntry(i, val));
                    colors.add(getColorForRating(val));
                }
            }

            if (entries.isEmpty()) {
                barChart.clear();
                barChart.setNoDataText(getString(R.string.no_recommendation_data));
                barChart.invalidate();
                chartStatusText.setText(R.string.no_recommendation_data);
                chartStatusText.setVisibility(View.VISIBLE);
                return;
            }

            BarDataSet dataSet = new BarDataSet(entries, "");
            dataSet.setColors(colors);
            dataSet.setDrawValues(false);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.8f);
            barChart.setData(barData);
            barChart.animateY(500);
            barChart.invalidate();
        });
    }

    private void showLoadError() {
        runOnUiThread(() -> {
            barChart.clear();
            barChart.invalidate();
            chartStatusText.setText(R.string.recommendation_load_failed);
            chartStatusText.setVisibility(View.VISIBLE);
        });
    }

    private int getColorForRating(float rating) {
        if (rating <= 2.0f) {
            return Color.RED;
        } else if (rating <= 4.0f) {
            return Color.rgb(255, 165, 0); // Orange
        } else if (rating <= 6.0f) {
            return Color.YELLOW;
        } else if (rating <= 8.0f) {
            return Color.rgb(173, 255, 47); // Yellow-Green
        } else {
            return Color.GREEN; // Bright Green
        }
    }

    private void showDetailPopup(int binIndex) {
        String[] ranges = {"0h-3h", "4h-7h", "8h-11h", "12h-15h", "16h-19h", "20h-23h"};
        if (binIndex < 0 || binIndex >= ranges.length) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_study_time_detail, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView title = dialogView.findViewById(R.id.detailTitle);
        BarChart detailChart = dialogView.findViewById(R.id.detailedBarChart);
        Button closeButton = dialogView.findViewById(R.id.closeDetailButton);

        if (title != null) title.setText(getString(R.string.performance_detail_title, ranges[binIndex]));

        if (detailChart != null) setupDetailChart(detailChart, binIndex);

        if (closeButton != null) closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupDetailChart(BarChart chart, int binIndex) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        
        int startHour = binIndex * 4;
        xAxis.setAxisMinimum(startHour - 0.5f);
        xAxis.setAxisMaximum(startHour + 3.5f);
        xAxis.setLabelCount(4);
        
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ((int) value) + "h";
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(10f);
        leftAxis.setLabelCount(11, true);

        chart.getAxisRight().setEnabled(false);

        controller.getDetailedAverages(allAverages -> {
            List<BarEntry> entries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                int hour = startHour + i;
                float val = allAverages[hour];
                
                float displayVal = val >= 0 ? val : 0f;
                entries.add(new BarEntry(hour, displayVal));
                // If no data, use a very light gray or transparent color
                colors.add(val >= 0 ? getColorForRating(val) : Color.parseColor("#EEEEEE"));
            }

            BarDataSet dataSet = new BarDataSet(entries, "");
            dataSet.setColors(colors);
            dataSet.setDrawValues(true); // Show the specific average rating on top of each detailed bar
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(Color.BLACK);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.85f); // Make these bars extra wide
            chart.setData(barData);
            chart.animateY(800);
            chart.invalidate();
        });
    }
}
