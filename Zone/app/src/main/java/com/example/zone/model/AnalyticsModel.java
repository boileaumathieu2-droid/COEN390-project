package com.example.zone.model;

import java.util.ArrayList;

public class AnalyticsModel {
    private final VirtualDatabase db;

    public AnalyticsModel() {
        this.db = new VirtualDatabase();
    }

    public interface HeartRateDataCallback {
        void onComplete(StudySessionModel session);
    }

    public void getLastSession(HeartRateDataCallback callback) {
        db.getStudySessions(sessions -> {
            if (sessions != null && !sessions.isEmpty()) {
                sessions.sort((s1, s2) -> {
                    if (s1.getStartTime() == null) return 1;
                    if (s2.getStartTime() == null) return -1;
                    return s2.getStartTime().compareTo(s1.getStartTime());
                });
                callback.onComplete(sessions.get(0));
            } else {
                callback.onComplete(null);
            }
        });
    }

    public int getLiveHeartRate() {
        StudySessionModel liveSession = StudySessionModel.getInstance();
        return (liveSession != null) ? liveSession.getHeartRateReading() : 0;
    }
}
