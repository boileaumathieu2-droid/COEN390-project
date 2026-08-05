package com.example.zone.controller;

import com.example.zone.model.AnalyticsModel;
import com.example.zone.model.StudySessionModel;

public class AnalyticsController {
    private final AnalyticsModel model;

    public AnalyticsController() {
        this.model = new AnalyticsModel();
    }

    public void getSessionData(AnalyticsModel.HeartRateDataCallback callback) {
        StudySessionModel liveSession = StudySessionModel.getInstance();
        if (liveSession != null && liveSession.isActive()) {
            callback.onComplete(liveSession);
        } else {
            model.getLastSession(callback);
        }
    }

    public int getCurrentHeartRate() {
        return model.getLiveHeartRate();
    }
}
