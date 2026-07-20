package com.example.zone.model;

public class TimerModel {

    private static TimerModel instance; // create a single instance
    private int studyDuration;   // total duration in seconds defined by user
    private int breakDuration;    // duration of break in seconds defined by user
    private boolean isRunning;  // true if the timer is currently running
    private boolean breakTime;    // true if there is a break after the study session
    private boolean breakEnabled; // keeps track of the break switch
    private int remainingTime;  // remaining time in seconds, used for the display
    private StudySessionModel session;  // use to get the live session



    // functions:

    private TimerModel() {
        // Default values: 25 minutes (1500 seconds), 5 minute break (300 seconds)
        this.studyDuration = 1500;
        this.remainingTime = 1500;
        this.breakDuration = 300;
        this.breakEnabled = false;
        this.breakTime = false;
    }

    public static TimerModel getInstance() {
        if (instance == null) {
            instance = new TimerModel(); // created once, the very first time
        }
        return instance; // every other time, returns the SAME object
    }

    // setters and getters
    public void setStudyDuration(int duration) {
        studyDuration = duration;
        if (!isRunning) {
            remainingTime = duration;
        }
    }

    public void setBreakDuration(int duration) {
        breakDuration = duration;
    }

    public void setBreakEnabled(boolean enabled) {
        breakEnabled = enabled;
    }

    public boolean isBreakEnabled() {
        return breakEnabled;
    }

    public boolean tick() {
        if (isRunning && remainingTime > 0) {
            remainingTime--;
            return true;
        }
        if (remainingTime <= 0) {
            isRunning = false;
            if (!breakTime && breakEnabled) {
                switchToBreak();
            } else {
                // If it was already a break, or no break is enabled, go back to study state
                breakTime = false;
                remainingTime = studyDuration;
            }
        }
        return false;
    }

    private void switchToBreak() {
        breakTime = true;
        remainingTime = breakDuration;
    }

    public void completeSession() {
        // finalize study session
        if(!breakTime){
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
            // reset timer duration in case
            if (remainingTime < studyDuration) {
                remainingTime = studyDuration;
            }
            isRunning = true;

            // create StudySessionModel object if study session
            if(!breakTime){
                session = new StudySessionModel();
                session.startSession(); // creates and starts the session
            }
        }
    }

    public void pauseTimer() {
        isRunning = false;
    }

    public void resumeTimer() {isRunning = true;}

    public void stopAndReset() {
        isRunning = false;
        breakTime = false;
        remainingTime = studyDuration;
        // end the live session tracker
        session.endSession(studyDuration - remainingTime);
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
