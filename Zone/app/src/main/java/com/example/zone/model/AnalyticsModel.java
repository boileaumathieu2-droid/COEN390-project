package com.example.zone.model;

import java.util.ArrayList;

public class AnalyticsModel {
    private final VirtualDatabase db;

    public AnalyticsModel() {
        this.db = new VirtualDatabase();
    }

    public interface HeartRateDataCallback {
        void onComplete(int[] data);
    }

    public void getLastSessionHeartRate(HeartRateDataCallback callback) {
        db.getStudySessions(sessions -> {
            if (sessions != null && !sessions.isEmpty()) {
                sessions.sort((s1, s2) -> {
                    if (s1.getStartTime() == null || s2.getStartTime() == null) return 0;
                    return s2.getStartTime().compareTo(s1.getStartTime());
                });
                callback.onComplete(sessions.get(0).getHeartRateData());
            } else {
                callback.onComplete(new int[0]);
            }
        });
    }

    public int getLiveHeartRate() {
        StudySessionModel liveSession = StudySessionModel.getInstance();
        return (liveSession != null) ? liveSession.getHeartRateReading() : 0;
    }
}
