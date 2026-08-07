package com.example.zone.controller;

import com.example.zone.model.AnalyticsModel;
import com.example.zone.model.StudySessionModel;
import com.example.zone.model.TimerModel;

public class AnalyticsController {
    private final AnalyticsModel model;

    public AnalyticsController() {
        this.model = new AnalyticsModel();
    }

    public void getSessionData(AnalyticsModel.HeartRateDataCallback callback) {
        TimerModel timer = TimerModel.getInstance();
        StudySessionModel liveSession = timer.getLiveSession();
        if (liveSession != null) {
            callback.onComplete(liveSession);
            return;
        }

        StudySessionModel completedSession = timer.getLastCompletedSession();
        if (completedSession != null) {
            callback.onComplete(completedSession);
            return;
        }

        model.getLastSession(callback);
    }

    public int getCurrentHeartRate() {
        return model.getLiveHeartRate();
    }
}
