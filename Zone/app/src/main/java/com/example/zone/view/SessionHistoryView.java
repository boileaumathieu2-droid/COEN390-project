package com.example.zone.view;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zone.R;
import com.example.zone.model.Database;
import com.example.zone.model.StudySessionModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SessionHistoryView extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SessionHistoryAdapter adapter;
    private List<StudySessionModel> sessionList;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_history);

        db = new Database(this);
        recyclerView = findViewById(R.id.sessionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        loadSessions();
    }

    private void loadSessions() {
        String username = getSharedPreferences("ZonePrefs", MODE_PRIVATE).getString("username", null);
        if (username != null) {
            int userID = db.getUserID(username);
            sessionList = db.getAllSessions(userID);
            adapter = new SessionHistoryAdapter(sessionList, this::showSessionDetail);
            recyclerView.setAdapter(adapter);
        }
    }

    private void showSessionDetail(StudySessionModel session) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_session_detail, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView title = dialogView.findViewById(R.id.detailTitle);
        TextView startEnd = dialogView.findViewById(R.id.detailStartEnd);
        TextView avgHR = dialogView.findViewById(R.id.detailAvgHR);
        TextView restingHR = dialogView.findViewById(R.id.detailRestingHR);
        TextView maxHR = dialogView.findViewById(R.id.detailMaxHR);
        TextView minHR = dialogView.findViewById(R.id.detailMinHR);
        TextView duration = dialogView.findViewById(R.id.detailDuration);
        TextView productivity = dialogView.findViewById(R.id.detailProductivity);
        TextView objectiveMet = dialogView.findViewById(R.id.detailObjectiveMet);
        LineChart chart = dialogView.findViewById(R.id.detailChart);
        Button closeButton = dialogView.findViewById(R.id.closeButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteButton);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        title.setText("Session: " + session.getStartTime().format(formatter));
        
        String endStr = session.getEndTime() != null ? session.getEndTime().format(formatter) : "N/A";
        startEnd.setText("Start: " + session.getStartTime().format(formatter) + "\nEnd: " + endStr);

        avgHR.setText(session.getHeartRate() + " BPM");
        restingHR.setText(session.getRestingHeartRate() + " BPM");
        maxHR.setText(session.getMaxHeartRate() + " BPM");
        minHR.setText(session.getMinHeartRate() + " BPM");
        
        int d = session.getDuration();
        duration.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", d / 3600, (d % 3600) / 60, d % 60));
        
        productivity.setText(session.getProductivityRating() + "/10");
        objectiveMet.setText(session.getObjectiveMet() ? "Yes" : "No");

        setupChart(chart, session.getHeartRateData());

        closeButton.setOnClickListener(v -> dialog.dismiss());
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Session")
                    .setMessage("Are you sure you want to delete this study session?")
                    .setPositiveButton("Delete", (dI, which) -> {
                        if (db.deleteSession(session.getId())) {
                            dialog.dismiss();
                            loadSessions(); // Refresh the list
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        dialog.show();
    }

    private void setupChart(LineChart chart, int[] heartRateData) {
        if (heartRateData == null || heartRateData.length == 0) {
            chart.setNoDataText("No heart rate data available.");
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < heartRateData.length; i++) {
            entries.add(new Entry(i, heartRateData[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Heart Rate");
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    private static class SessionHistoryAdapter extends RecyclerView.Adapter<SessionHistoryAdapter.ViewHolder> {
        private final List<StudySessionModel> sessions;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(StudySessionModel session);
        }

        SessionHistoryAdapter(List<StudySessionModel> sessions, OnItemClickListener listener) {
            this.sessions = sessions;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StudySessionModel session = sessions.get(position);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            holder.dateTimeText.setText(session.getStartTime().format(formatter));
            
            int d = session.getDuration();
            String summary = String.format(Locale.getDefault(), "Duration: %02d:%02d | Avg HR: %d", (d % 3600) / 60, d % 60, session.getHeartRate());
            holder.summaryText.setText(summary);
            
            holder.itemView.setOnClickListener(v -> listener.onItemClick(session));
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView dateTimeText;
            TextView summaryText;

            ViewHolder(View itemView) {
                super(itemView);
                dateTimeText = itemView.findViewById(R.id.sessionDateTime);
                summaryText = itemView.findViewById(R.id.sessionSummary);
            }
        }
    }
}