package com.example.zone.model;

import java.time.LocalDateTime;
import java.time.Duration;

public class StudySessionModel {

    public enum Status {
        INACTIVE,
        ACTIVE,
        COMPLETE
    }

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int duration;
    private Status status;
    // TODO: make setters and getters for the following attributes
    private int restingHeartRate;   // take heart rate value at time 0
    private String objective = "";  // default case
    private B
    private int productivityRating; // rated on session completion
    private int[] heartRateData;    // save the heart rate values every 15 seconds
    private int currentHeartRate;

    // attributes for functions to use and keep track

    // Constructor
    public StudySessionModel(String objective) {    // constructed at start of session
        startTime = LocalDateTime.now();
        duration = 0;
        status = Status.INACTIVE;
        this.objective = objective; // input is objective for session
    }

    public void startSession() {
        startTime = LocalDateTime.now();
        status = Status.ACTIVE;
    }

    // Complete session
    public void completeSession(int[] heartRateData) { // save the duration and end time
        endTime = LocalDateTime.now();
        if (startTime != null) {
            duration = (int) Duration.between(startTime, endTime).getSeconds();
            this.heartRateData = heartRateData;
            this.restingHeartRate = heartRateData[0];
        }
        status = Status.COMPLETE;
    }

    // Getters
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getDuration() {
        return duration;
    }

    public Status getStatus() {
        return status;
    }

    public String getObjective() {
        return objective;
    }

    public int getProductivityRating() {
        return productivityRating;
    }

    public int[] getHeartRateData() {
        return heartRateData;
    }

    public int getRestingHeartRate() {
        return restingHeartRate;
    }
    public int getHeartRate() {
        return currentHeartRate;
    }

    // setters
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public void setObjective(String objective) {
        this.objective = objective;
    }

    public void setProductivityRating(int productivityRating) {
        this.productivityRating = productivityRating;
    }

    public void setHeartRateData(int[] heartRateData) {
        this.heartRateData = heartRateData;
    }

    public void setRestingHeartRate(int restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }
    public void setHeartRate(int currentHeartRate) {
        this.currentHeartRate = currentHeartRate;
    }
    public boolean isActive() { // when the session is running
        return status == Status.ACTIVE;
    }

    // functions in case we want to take into account the paused time, for not don't use...

//    public boolean isPaused() {
//        return status == Status.INACTIVE;
//    }
//    public boolean isResumed() {
//        return status == Status.ACTIVE;
//    }
    public void endSession(int duration) {  // at the end of the session, this triggers
        if (status != Status.ACTIVE || duration < 0) {
            return;
        }

        this.duration = duration;
        this.endTime = LocalDateTime.now();
        this.status = Status.COMPLETE;
    }
}
