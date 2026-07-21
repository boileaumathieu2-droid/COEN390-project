package com.example.zone.model;

import static java.sql.Types.NULL;

import java.time.LocalDateTime;
import java.time.Duration;

public class StudySessionModel {

    public enum Status {
        INACTIVE,
        ACTIVE,
        COMPLETE
    }
     private static StudySessionModel instance;


    private class heartRateInstances {
        private int index;
        private int heartRate;

        // constructor
        public heartRateInstances(int index, int heartRate) {
            this.index = index;
            this.heartRate = heartRate;
        }
        // setters and getters
        public int getIndex() {
            return index;
        }
        public int getHeartRate() {
            return heartRate;
        }
        public void setIndex(int index) {
            this.index = index;
        }
        public void setHeartRate(int heartRate) {
            this.heartRate = heartRate;
        }
    }

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int duration;
    private Status status;
    private int restingHeartRate;   // take heart rate value at time 0
    private String objective = "";  // default case
    private Boolean objectiveMet = false;   // default, rating at session end
    private int productivityRating; // rated on session completion
    private int[] heartRateData;    // save the heart rate values every 15 seconds
    private int currentHeartRate;
    // Peak and valley heart rate values and times
    private heartRateInstances maxHeartRate;
    private heartRateInstances minHeartRate;


    // attributes for functions to use and keep track

    // Constructor
    public StudySessionModel() {    // constructed at start of session
        startTime = LocalDateTime.now();
        duration = 0;
        status = Status.INACTIVE;
    }
    public static StudySessionModel getInstance() {

        if(instance == null) {
            instance = new StudySessionModel();
        }

        return instance;
    }

    public void startSession() {
        startTime = LocalDateTime.now();
        status = Status.ACTIVE;
    }

    // Complete session logs all the session data
    public void completeSession(int[] heartRateData, Boolean objectiveMet, int productivityRating) { // save the duration and end time
        endTime = LocalDateTime.now();
        if (startTime != null) {
            // save the session data
            duration = (int) Duration.between(startTime, endTime).getSeconds();
            this.heartRateData = heartRateData;
            this.restingHeartRate = (heartRateData[0] + heartRateData[1])/2;    // take an average on early values
            this.objectiveMet = objectiveMet;

            // set productivity rating
            if(0 <= productivityRating && productivityRating <= 10) {
                this.productivityRating = productivityRating;   // user rating
            } else {
                this.productivityRating = NULL; // make sure to avoid using the empty value in averages
            }

            // calculate average heart rate
            int sum = 0;
            int max = 0;
            int maxIndex = 0;
            int min = 240;
            int minIndex = 0;
            for (int i = 0; i < heartRateData.length; i++) {
                // calculate average
                sum += heartRateData[i];

                // verify peak and valley in the process
                if (heartRateData[i] > max) {
                    max = heartRateData[i];
                    maxIndex = i;
                }
                if (heartRateData[i] < min) {
                    min = heartRateData[i];
                    minIndex = i;
                }
            }
            currentHeartRate = sum / heartRateData.length;

            // log the max and min heart rates
            this.maxHeartRate = new heartRateInstances(maxIndex, max);
            this.minHeartRate = new heartRateInstances(minIndex, min);
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
    public Boolean getObjectiveMet() {
        return objectiveMet;
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
    public int getMaxHeartRate() {
        return maxHeartRate.getHeartRate();
    }
    public int getMinHeartRate() {
        return minHeartRate.getHeartRate();
    }
    public int getMaxHeartRateIndex() {
        return maxHeartRate.getIndex();
    }
    public int getMinHeartRateIndex() {
        return minHeartRate.getIndex();
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
    public void setObjectiveMet(Boolean objectiveMet) {
        this.objectiveMet = objectiveMet;
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
    public void setMaxHeartRate(int maxHeartRate) {
        this.maxHeartRate.setHeartRate(maxHeartRate);
    }
    public void setMinHeartRate(int minHeartRate) {
        this.minHeartRate.setHeartRate(minHeartRate);
    }
    public void setMaxHeartRateIndex(int maxHeartRateIndex) {
        this.maxHeartRate.setIndex(maxHeartRateIndex);
    }
    public void setMinHeartRateIndex(int minHeartRateIndex) {
        this.minHeartRate.setIndex(minHeartRateIndex);
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
