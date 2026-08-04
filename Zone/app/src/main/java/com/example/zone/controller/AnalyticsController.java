package com.example.zone.controller;

import com.example.zone.model.AnalyticsModel;
import com.example.zone.model.StudySessionModel;

public class AnalyticsController {
    private final AnalyticsModel model;

    public AnalyticsController() {
        this.model = new AnalyticsModel();
    }

    public void getHeartRateData(AnalyticsModel.HeartRateDataCallback callback) {
        StudySessionModel liveSession = StudySessionModel.getInstance();
        if (liveSession != null && liveSession.isActive()) {
            callback.onComplete(liveSession.getHeartRateData());
        } else {
            model.getLastSessionHeartRate(callback);
        }
    }

    public int getCurrentHeartRate() {
        return model.getLiveHeartRate();
    }
}
