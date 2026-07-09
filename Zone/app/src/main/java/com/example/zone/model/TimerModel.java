package com.example.zone.model;

public class TimerModel {
    private int studyDuration;   // total duration in seconds defined by user
    private int breakDuration;    // duration of break in seconds defined by user
    private boolean isRunning;  // true if the timer is currently running
    private boolean breakTime;    // true if it is time for a break
    private int remainingTime;  // remaining time in seconds, used for the display


    // functions:

    public void setStudyDuration(int duration) {
        studyDuration = duration;
    }

    public void setBreakDuration(int duration) {
        breakDuration = duration;
    }

    public void startTimer() {
        if (!isRunning)
            isRunning = true;
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

    public int getMinutes(){
        return (remainingTime / 60);
    }

    public int getSeconds(){
        return (remainingTime % 60);
    }

    public boolean isBreakTime(){
        return breakTime;
    }



}
