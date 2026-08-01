package com.example.zone.model;

import android.content.Context;

public class RecommendedStudyTimesModel {
    private final Database db;
    private final int userID;

    public RecommendedStudyTimesModel(Context context, int userID) {
        this.db = new Database(context);
        this.userID = userID;
    }

    public float[] getAverageRatingsByHour() {
        float[] averages = new float[24];
        int[] counts = new int[24];
        float[] totals = new float[24];

        // We'll fetch all sessions for the user and process them here
        // or add a specific query to the Database class.
        // For now, let's assume we fetch them and aggregate.
        
        java.util.List<StudySessionModel> sessions = db.getAllSessions(userID);
        
        for (StudySessionModel session : sessions) {
            // Include ratings in the range 0-10. -1 indicates no rating.
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
                averages[i] = -1f; // -1 indicates no data for this hour
            }
        }

        return averages;
    }
}
