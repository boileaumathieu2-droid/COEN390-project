package com.example.zone.controller;

import android.content.Context;
import com.example.zone.model.RecommendedStudyTimesModel;

public class RecommendedStudyTimesController {
    private final RecommendedStudyTimesModel model;

    public RecommendedStudyTimesController(Context context, int userID) {
        this.model = new RecommendedStudyTimesModel(context, userID);
    }

    public float[] getHourlyAverages() {
        return model.getAverageRatingsByHour();
    }
}
