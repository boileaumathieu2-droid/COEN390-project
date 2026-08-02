package com.example.zone.model;

import android.content.SharedPreferences;
import android.content.Context;


public class TimerModel {

    private SharedPreferences prefs;


    private static TimerModel instance; // create a single instance
    private int studyDuration;   // total duration in seconds defined by user
    private int breakDuration;    // duration of break in seconds defined by user
    private boolean isRunning;  // true if the timer is currently running
    private boolean breakTime;    // true if there is a break after the study session
    private boolean breakEnabled; // keeps track of the break switch
    private int remainingTime;  // remaining time in seconds, used for the display
    private StudySessionModel session;


    private TimerModel(Context context) {
        prefs = context.getSharedPreferences(
                "timer_settings",
                Context.MODE_PRIVATE
        );
        this.studyDuration = prefs.getInt("studyDuration", 1500);
        this.breakDuration = prefs.getInt("breakDuration", 300);
        this.breakEnabled = prefs.getBoolean("breakEnabled", false);
        this.breakTime = false;
        this.remainingTime = this.studyDuration;
    }

    public static TimerModel getInstance(Context context) {
        if (instance == null) {
            instance = new TimerModel(context); // created once, the very first time
        }
        return instance; // every other time, returns the SAME object
    }
    // setters and getters
    public void setStudyDuration(int duration) {
        studyDuration = Math.max(1, duration);

        if (!isRunning) {
            remainingTime = studyDuration;
        }
        prefs.edit()
                .putInt("studyDuration", studyDuration)
                .apply();
    }
    public void setBreakDuration(int duration) {
        breakDuration = Math.max(1, duration);
        prefs.edit()
                .putInt("breakDuration", breakDuration)
                .apply();
    }
    public void setBreakEnabled(boolean enabled) {
        breakEnabled = enabled;
        prefs.edit()
                .putBoolean("breakEnabled", breakEnabled)
                .apply();
    }
    public boolean isBreakEnabled() {
        return breakEnabled;
    }
    public boolean tick() {
        if (!isRunning) {
            return false;
        }

        if (remainingTime > 0) {
            remainingTime--;
        }

        if (remainingTime > 0) {
            return true;
        }

        finishCurrentPeriod();
        return false;
    }

    private void finishCurrentPeriod() {
        isRunning = false;
        if (!breakTime && session != null) {
            session.endSession(studyDuration);
        }

        if (!breakTime && breakEnabled) {
            switchToBreak();
        } else {
            // If it was already a break, or no break is enabled, go back to study state.
            breakTime = false;
            remainingTime = studyDuration;
            session = null;
        }
    }

    private void switchToBreak() {
        breakTime = true;
        remainingTime = breakDuration;
    }

    public void completeSession() {
        // finalize study session
        if (!breakTime && session != null) {
            session.endSession(studyDuration - remainingTime);
        }


        isRunning = false;
        if (!breakTime && breakEnabled) {
            switchToBreak();
        } else {
            // Already in break or no break enabled: reset to study
            breakTime = false;
            remainingTime = studyDuration;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    // interract with start/stop

    public void startTimer() { // take the objective to build the studySessionModel
        if (!isRunning) {
            int currentDuration = breakTime ? breakDuration : studyDuration;
            if (remainingTime <= 0 || remainingTime > currentDuration) {
                remainingTime = currentDuration;
            }
            isRunning = true;

            // create StudySessionModel object if study session
            if(!breakTime){
                // The BLE manager writes into this shared session instance.
                // Creating a separate object here used to discard every sensor
                // reading received from the connectivity page.
                session = StudySessionModel.getInstance();
                session.startSession(); // creates and starts the session
            }
        }
    }

    public void pauseTimer() {
        isRunning = false;
    }

    public void resumeTimer() {
        if (remainingTime > 0) {
            isRunning = true;
        }
    }

    public void stopAndReset() {
        isRunning = false;
        if (session != null) {
            session.endSession(Math.max(0, studyDuration - remainingTime));
        }
        breakTime = false;
        remainingTime = studyDuration;
        session = null;
    }

    public void resetTimer() {
        // set the correct amount of time depending on the state
        if(breakTime){
            remainingTime = breakDuration;
        }
        else{
            remainingTime = studyDuration;
        }
    }

    // variables useful to display the remaining time
    public int getMinutes(){
        return (remainingTime / 60);
    }

    public int getSeconds(){
        return (remainingTime % 60);
    }

    public boolean isBreakTime(){
        return breakTime;
    }

    public int getStudyDuration(){
        return studyDuration;
    }
    public int getBreakDuration(){
        return breakDuration;
    }
    public StudySessionModel getLiveSession(){
        return session;
    }

}
