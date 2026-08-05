package com.example.zone.view;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.github.mikephil.charting.formatter.ValueFormatter;

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
        barChart.setExtraBottomOffset(8f);
        barChart.getLegend().setTextColor(Color.DKGRAY);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(8, false);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int hour = (int) value;
                // Only show labels for these specific hours
                switch (hour) {
                    case 0:
                    case 3:
                    case 7:
                    case 11:
                    case 15:
                    case 19:
                    case 23:
                        return hour + "h";
                    default:
                        return "";
                }
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

            for (int i = 0; i < 24; i++) {
                float val = averages != null && i < averages.length ? averages[i] : -1f;

                // Only add entries where we have data (val >= 0)
                if (val >= 0) {
                    entries.add(new BarEntry(i, val));

                    // Color coding based on value (Scale 0-10)
                    if (val <= 2.0f) {
                        colors.add(Color.RED);
                    } else if (val <= 4.0f) {
                        colors.add(Color.rgb(255, 165, 0)); // Orange
                    } else if (val <= 6.0f) {
                        colors.add(Color.YELLOW);
                    } else if (val <= 8.0f) {
                        colors.add(Color.rgb(173, 255, 47)); // Yellow-Green
                    } else {
                        colors.add(Color.GREEN); // Bright Green
                    }
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

            chartStatusText.setVisibility(View.GONE);

            BarDataSet dataSet = new BarDataSet(entries, "Average Productivity Rating");
            dataSet.setColors(colors);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setValueTextSize(10f);

            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.72f);
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
}
