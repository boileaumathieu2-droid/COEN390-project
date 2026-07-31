package com.example.zone.view;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zone.R;
import com.example.zone.controller.Recommended_Study_Times_Controller;
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

public class Recommended_Study_Times_View extends AppCompatActivity {

    private Recommended_Study_Times_Controller controller;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommended_study_times);

        barChart = findViewById(R.id.performanceBarChart);
        Button backButton = findViewById(R.id.backToAnalyticsButton);
        Button mainMenuButton = findViewById(R.id.mainMenuButton);

        backButton.setOnClickListener(v -> finish());
        mainMenuButton.setOnClickListener(v -> {
            // Logic to return to main menu (might need to clear task stack)
            finish(); 
        });

        // Initialize session and get user ID
        Session.init(getApplicationContext());
        int userID = Session.getUserID();

        controller = new Recommended_Study_Times_Controller(this, userID);
        setupChart();
        displayData();
    }

    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(24);
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
        leftAxis.setAxisMaximum(5f); // Assuming rating is 1-5
        leftAxis.setLabelCount(6, true);
        leftAxis.setDrawGridLines(true);

        barChart.getAxisRight().setEnabled(false);
    }

    private void displayData() {
        float[] averages = controller.getHourlyAverages();
        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (int i = 0; i < 24; i++) {
            float val = averages[i];
            entries.add(new BarEntry(i, val));
            
            // Color coding based on value
            if (val <= 1.0f) {
                colors.add(Color.RED);
            } else if (val <= 2.0f) {
                colors.add(Color.rgb(255, 165, 0)); // Orange
            } else if (val <= 3.0f) {
                colors.add(Color.YELLOW);
            } else if (val <= 4.0f) {
                colors.add(Color.rgb(173, 255, 47)); // Yellow-Green (GreenYellow)
            } else {
                colors.add(Color.GREEN); // Bright Green
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Average Productivity Rating");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }
}
