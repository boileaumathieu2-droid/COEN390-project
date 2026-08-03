package com.example.zone.controller;

import android.content.Context;
import com.example.zone.model.RecommendedStudyTimesModel;

public class RecommendedStudyTimesController {
    private final RecommendedStudyTimesModel model;

    public RecommendedStudyTimesController(Context context) {
        this.model = new RecommendedStudyTimesModel(context);
    }

    public void getHourlyAverages(RecommendedStudyTimesModel.HourlyAveragesCallback callback) {
        model.getAverageRatingsByHour(callback);
    }
}
