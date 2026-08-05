package com.example.zone.model;

import android.content.Context;
import java.util.ArrayList;

public class RecommendedStudyTimesModel {
    private final VirtualDatabase db;

    public RecommendedStudyTimesModel(Context context) {
        this.db = new VirtualDatabase();
    }

    public interface HourlyAveragesCallback {
        void onComplete(float[] averages);
    }

    public void getAverageRatingsByHour(HourlyAveragesCallback callback) {
        db.getStudySessions(sessions -> {
            float[] averages = new float[6];
            int[] counts = new int[6];
            float[] totals = new float[6];

            for (StudySessionModel session : sessions) {
                // Include ratings in the range 0-10. -1 indicates no rating.
                if (session.getStartTime() != null && session.getProductivityRating() >= 0) {
                    int hour = session.getStartTime().getHour();
                    int bin = hour / 4; // 6 bins of 4 hours each
                    totals[bin] += session.getProductivityRating();
                    counts[bin]++;
                }
            }

            for (int i = 0; i < 6; i++) {
                if (counts[i] > 0) {
                    averages[i] = totals[i] / counts[i];
                } else {
                    averages[i] = -1f; // -1 indicates no data for this bin
                }
            }
            callback.onComplete(averages);
        });
    }

    public void getDetailedHourlyAverages(HourlyAveragesCallback callback) {
        db.getStudySessions(sessions -> {
            float[] averages = new float[24];
            int[] counts = new int[24];
            float[] totals = new float[24];

            for (StudySessionModel session : sessions) {
                if (session.getStartTime() != null && session.getProductivityRating() >= 0) {
                    int hour = session.getStartTime().getHour();
                    totals[hour] += session.getProductivityRating();
                    counts[hour]++;
                }
            }

            for (int i = 0; i < 24; i++) {
                if (counts[i] > 0) {
                    averages[i] = totals[i] / counts[i];
                } else {
                    averages[i] = -1f;
                }
            }
            callback.onComplete(averages);
        });
    }
}
