package com.example.zone.model;

import android.content.Context;
import android.content.SharedPreferences;

public class TimerSettingsModel {

    private static final String PREF_NAME = "TimerSettings";
    private static final String STUDY_DURATION_KEY = "study_duration";
    private static final String STUDY_BREAK_KEY = "study_break";
    private static final String BREAK_ENABLED_KEY = "break_enabled";

    private int StudyDuration;
    private int StudyBreak;
    private boolean BreakEnabled;

    private SharedPreferences preferences;

    public TimerSettingsModel(Context context) {
        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
        StudyDuration = preferences.getInt(STUDY_DURATION_KEY, 25 * 60);
        StudyBreak = preferences.getInt(STUDY_BREAK_KEY, 5 * 60);
        BreakEnabled = preferences.getBoolean(BREAK_ENABLED_KEY, false);
    }


    public void setStudyDuration(int duration) {
        StudyDuration = duration;
        preferences.edit()
                .putInt(STUDY_DURATION_KEY, (int) duration)
                .apply();
    }


    public void setStudyBreak(int duration) {
        StudyBreak = duration;
        preferences.edit()
                .putInt(STUDY_BREAK_KEY, (int) duration)
                .apply();
    }
    public void setBreakEnabled(boolean enable) {
        BreakEnabled = enable;
        preferences.edit()
                .putBoolean(BREAK_ENABLED_KEY, enable)
                .apply();
    }
    public int getStudyDuration() {
        return StudyDuration;
    }
    public int getBreakDuration() {
        return StudyBreak;
    }
    public boolean isBreakEnabled() {
        return BreakEnabled;
    }
    public void setTimerSettings(Context context) {
        TimerSettingsModel model = getTimerSettings(context);
        int studyTime = model.getStudyDuration();
        int breakTime = model.getBreakDuration();
        boolean enabled = model.isBreakEnabled();
        TimerModel timer = TimerModel.getInstance();
        timer.setBreakEnabled(enabled);
        timer.setBreakDuration(breakTime);
        timer.setStudyDuration(studyTime);
    }
    public TimerSettingsModel getTimerSettings(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                "timer_preferences",
                Context.MODE_PRIVATE
        );
        TimerSettingsModel model = new TimerSettingsModel(context);
        model.setStudyDuration(preferences.getInt("study_duration", 25));
        model.setStudyBreak(preferences.getInt("study_break", 5));
        model.setBreakEnabled(preferences.getBoolean("break_enabled", true));

        return model;
    }
}