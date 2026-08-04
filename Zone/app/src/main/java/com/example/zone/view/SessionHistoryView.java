package com.example.zone.view;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zone.R;
import com.example.zone.model.StudySessionModel;
import com.example.zone.model.VirtualDatabase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SessionHistoryView extends AppCompatActivity {

    private enum Range { WEEK, MONTH, SIX_MONTHS, ALL }

    private final List<StudySessionModel> allSessions = new ArrayList<>();
    private final List<StudySessionModel> shownSessions = new ArrayList<>();
    private final VirtualDatabase database = new VirtualDatabase();

    private SessionHistoryAdapter adapter;
    private TextView emptyText;
    private Button weekButton;
    private Button monthButton;
    private Button sixMonthsButton;
    private Button allButton;
    private Range selectedRange = Range.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_history);

        RecyclerView recyclerView = findViewById(R.id.sessionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionHistoryAdapter(shownSessions, this::showSessionDetail);
        recyclerView.setAdapter(adapter);
        emptyText = findViewById(R.id.historyEmptyText);

        weekButton = findViewById(R.id.historyWeekButton);
        monthButton = findViewById(R.id.historyMonthButton);
        sixMonthsButton = findViewById(R.id.historySixMonthsButton);
        allButton = findViewById(R.id.historyAllButton);
        weekButton.setOnClickListener(view -> selectRange(Range.WEEK));
        monthButton.setOnClickListener(view -> selectRange(Range.MONTH));
        sixMonthsButton.setOnClickListener(view -> selectRange(Range.SIX_MONTHS));
        allButton.setOnClickListener(view -> selectRange(Range.ALL));
        findViewById(R.id.backButton).setOnClickListener(view -> finish());

        updateRangeButtons();
        loadSessions();
    }

    private void loadSessions() {
        emptyText.setText("Loading sessions…");
        emptyText.setVisibility(View.VISIBLE);
        database.getStudySessions(sessions -> {
            allSessions.clear();
            allSessions.addAll(sessions);
            allSessions.sort((left, right) -> {
                if (left.getStartTime() == null) return 1;
                if (right.getStartTime() == null) return -1;
                return right.getStartTime().compareTo(left.getStartTime());
            });
            applyRange();
        });
    }

    private void selectRange(Range range) {
        selectedRange = range;
        updateRangeButtons();
        applyRange();
    }

    private void updateRangeButtons() {
        weekButton.setEnabled(selectedRange != Range.WEEK);
        monthButton.setEnabled(selectedRange != Range.MONTH);
        sixMonthsButton.setEnabled(selectedRange != Range.SIX_MONTHS);
        allButton.setEnabled(selectedRange != Range.ALL);
    }

    private void applyRange() {
        LocalDateTime cutoff = null;
        LocalDateTime now = LocalDateTime.now();
        if (selectedRange == Range.WEEK) {
            cutoff = now.minusWeeks(1);
        } else if (selectedRange == Range.MONTH) {
            cutoff = now.minusMonths(1);
        } else if (selectedRange == Range.SIX_MONTHS) {
            cutoff = now.minusMonths(6);
        }

        shownSessions.clear();
        for (StudySessionModel session : allSessions) {
            if (cutoff == null || (session.getStartTime() != null
                    && !session.getStartTime().isBefore(cutoff))) {
                shownSessions.add(session);
            }
        }
        adapter.notifyDataSetChanged();
        boolean empty = shownSessions.isEmpty();
        emptyText.setText("No saved sessions in this time range.");
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showSessionDetail(StudySessionModel session) {
        if (session == null || session.getStartTime() == null) {
            return;
        }

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_session_detail, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "MMM dd, yyyy HH:mm",
                Locale.getDefault()
        );
        ((TextView) view.findViewById(R.id.detailTitle)).setText(
                "Session: " + session.getStartTime().format(formatter)
        );
        String end = session.getEndTime() == null
                ? "N/A" : session.getEndTime().format(formatter);
        ((TextView) view.findViewById(R.id.detailStartEnd)).setText(
                "Start: " + session.getStartTime().format(formatter) + "\nEnd: " + end
        );
        setMetric(view, R.id.detailAvgHR, bpm(session.getHeartRate()));
        setMetric(view, R.id.detailRestingHR, bpm(session.getRestingHeartRate()));
        setMetric(view, R.id.detailMaxHR, bpm(session.getMaxHeartRate()));
        setMetric(view, R.id.detailMinHR, bpm(session.getMinHeartRate()));

        int duration = session.getDuration();
        setMetric(view, R.id.detailDuration, String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                duration / 3600,
                (duration % 3600) / 60,
                duration % 60
        ));
        setMetric(
                view,
                R.id.detailProductivity,
                session.getProductivityRating() < 0
                        ? "Not rated" : session.getProductivityRating() + "/10"
        );
        setMetric(
                view,
                R.id.detailObjectiveMet,
                Boolean.TRUE.equals(session.getObjectiveMet()) ? "Yes" : "No"
        );
        setupChart(view.findViewById(R.id.detailChart), session.getHeartRateData());

        view.findViewById(R.id.closeButton).setOnClickListener(button -> dialog.dismiss());
        view.findViewById(R.id.deleteButton).setOnClickListener(button ->
                confirmDelete(session, dialog));
        dialog.show();
    }

    private String bpm(int value) {
        return value > 0 ? value + " BPM" : "No data";
    }

    private void setMetric(View root, int id, String value) {
        ((TextView) root.findViewById(id)).setText(value);
    }

    private void confirmDelete(StudySessionModel session, AlertDialog detailDialog) {
        new AlertDialog.Builder(this)
                .setTitle("Delete session")
                .setMessage("This session and its heart-rate data will be removed permanently.")
                .setPositiveButton("Delete", (dialog, which) ->
                        database.deleteStudySession(session.getDocumentId(), success -> {
                            if (success) {
                                detailDialog.dismiss();
                                allSessions.remove(session);
                                applyRange();
                                Toast.makeText(
                                        this,
                                        "Session deleted",
                                        Toast.LENGTH_SHORT
                                ).show();
                            } else {
                                Toast.makeText(
                                        this,
                                        "Could not delete session",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupChart(LineChart chart, int[] heartRates) {
        if (heartRates == null || heartRates.length == 0) {
            chart.clear();
            chart.setNoDataText("No heart-rate data was recorded.");
            chart.invalidate();
            return;
        }
        List<Entry> entries = new ArrayList<>();
        for (int index = 0; index < heartRates.length; index++) {
            entries.add(new Entry(index, heartRates[index]));
        }
        LineDataSet set = new LineDataSet(entries, "Heart Rate");
        set.setColor(Color.rgb(216, 50, 90));
        set.setCircleColor(Color.rgb(216, 50, 90));
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setDrawValues(false);
        chart.setData(new LineData(set));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    private static final class SessionHistoryAdapter
            extends RecyclerView.Adapter<SessionHistoryAdapter.ViewHolder> {

        interface OnItemClickListener {
            void onItemClick(StudySessionModel session);
        }

        private final List<StudySessionModel> sessions;
        private final OnItemClickListener listener;

        SessionHistoryAdapter(
                List<StudySessionModel> sessions,
                OnItemClickListener listener
        ) {
            this.sessions = sessions;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_session_history, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StudySessionModel session = sessions.get(position);
            if (session.getStartTime() == null) {
                holder.dateTime.setText("Unknown date");
            } else {
                holder.dateTime.setText(session.getStartTime().format(
                        DateTimeFormatter.ofPattern(
                                "MMM dd, yyyy HH:mm",
                                Locale.getDefault()
                        )
                ));
            }
            int duration = session.getDuration();
            holder.summary.setText(String.format(
                    Locale.getDefault(),
                    "Duration: %02d:%02d  •  Avg HR: %s",
                    duration / 60,
                    duration % 60,
                    session.getHeartRate() > 0
                            ? session.getHeartRate() + " BPM" : "No data"
            ));
            holder.itemView.setOnClickListener(view -> listener.onItemClick(session));
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        static final class ViewHolder extends RecyclerView.ViewHolder {
            final TextView dateTime;
            final TextView summary;

            ViewHolder(View view) {
                super(view);
                dateTime = view.findViewById(R.id.sessionDateTime);
                summary = view.findViewById(R.id.sessionSummary);
            }
        }
    }
}
