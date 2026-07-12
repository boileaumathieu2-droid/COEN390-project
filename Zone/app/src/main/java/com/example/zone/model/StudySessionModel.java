package com.example.zone.model;

public class StudySessionModel {

    public enum Status {
        INACTIVE,
        ACTIVE,
        COMPLETE
    }

    private long startTime;
    private long endTime;
    private int duration;
    private Status status;

    // Constructor
    public StudySessionModel() {
        startTime = 0;
        endTime = 0;
        duration = 0;
        status = Status.INACTIVE;
    }

    public void startSession() {
        startTime = System.currentTimeMillis();
        status = Status.ACTIVE;
    }

    // Complete session
    public void completeSession() {
        endTime = System.currentTimeMillis();
        duration = (int)((endTime - startTime) / 1000); // duration in seconds
        status = Status.COMPLETE;
    }

    // Getters
    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getDuration() {
        return duration;
    }

    public Status getStatus() {
        return status;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public boolean isActive() {
        return status == Status.ACTIVE;
    }
    public void endSession(int duration) {
        if (status != Status.ACTIVE || duration < 0) {
            return;
        }

        this.duration = duration;
        this.endTime = System.currentTimeMillis();
        this.status = Status.COMPLETE;
    }
}
