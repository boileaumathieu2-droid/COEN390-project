package com.example.zone.model;

public class TimerModel {

    private static TimerModel instance; // create a single instance
    private int studyDuration;   // total duration in seconds defined by user
    private int breakDuration;    // duration of break in seconds defined by user
    private boolean isRunning;  // true if the timer is currently running
    private boolean breakTime;    // true if there is a break after the study session
    private boolean breakEnabled; // true if the break timer is enabled
    private int remainingTime;  // remaining time in seconds, used for the display


    // functions:

    private TimerModel() {
        // default constructor
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

    public void setBreakTime(boolean isBreak) {
        breakTime = isBreak;
    }

    // interract with start/stop

    public void startTimer() {
        if (!isRunning)
        {
            if (breakTime) {
                remainingTime = breakDuration;
            } else {
                remainingTime = studyDuration;
            }
            isRunning = true;
        }


    }

    public void pauseTimer() {
        isRunning = false;
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

}
