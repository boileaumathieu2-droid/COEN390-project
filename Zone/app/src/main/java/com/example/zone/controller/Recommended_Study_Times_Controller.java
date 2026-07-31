package com.example.zone.controller;

import android.content.Context;
import com.example.zone.model.Recommended_Study_Times_Model;

public class Recommended_Study_Times_Controller {
    private final Recommended_Study_Times_Model model;

    public Recommended_Study_Times_Controller(Context context, int userID) {
        this.model = new Recommended_Study_Times_Model(context, userID);
    }

    public float[] getHourlyAverages() {
        return model.getAverageRatingsByHour();
    }
}
