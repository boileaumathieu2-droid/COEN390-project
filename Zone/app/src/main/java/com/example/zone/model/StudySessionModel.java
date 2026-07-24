package com.example.zone.model;

import static java.sql.Types.NULL;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class StudySessionModel {

    public enum Status {
        INACTIVE,
        ACTIVE,
        COMPLETE
    }
     private static StudySessionModel instance;


    public static class HeartRateInstance {
        private int index;
        private int heartRate;

        // constructor
        public HeartRateInstance(int index, int heartRate) {
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
    private int id;
    private int duration;
    private Status status;
    private int restingHeartRate;   // take heart rate value at time 0
    private Boolean objectiveMet = false;   // default, rating at session end
    private int productivityRating; // rated on session completion
    private List<Integer> heartRateDataList = new ArrayList<>();    // save the heart rate values every 15 seconds
    private int averageHeartRate;
    private HeartRateReading currentHeartRateReading;
    // Peak and valley heart rate values and times
    private HeartRateInstance maxHeartRate;
    private HeartRateInstance minHeartRate;


    // attributes for functions to use and keep track

    // Constructor
    public StudySessionModel() {    // constructed at start of session
        duration = 0;
        status = Status.INACTIVE;
    }
    public static StudySessionModel getInstance() {

        if(instance == null) {
            instance = new StudySessionModel();
        }

        return instance;
    }
    // function that gets the current heart rate value from HeartRateMonitorView.java and HeartRateReading.java
    public int getHeartRateReading() {
        if (currentHeartRateReading != null && currentHeartRateReading.hasGoodSignal()) {
            return currentHeartRateReading.getBpm();
        }
        return 0;
    }
    public void startSession() {
        startTime = LocalDateTime.now();
        status = Status.ACTIVE;
        restingHeartRate = getHeartRateReading();
        // I want to get the heart rate readings saved every 5 seconds (starting at t = 0) in t
    }
    public void addHeartRateReading(){
        int heartRate = getHeartRateReading();
        if (heartRate > 0) {
            heartRateDataList.add(heartRate);

            // initialize max/min if they are null
            if (maxHeartRate == null) {
                maxHeartRate = new HeartRateInstance(heartRateDataList.size() - 1, heartRate);
            }
            if (minHeartRate == null) {
                minHeartRate = new HeartRateInstance(heartRateDataList.size() - 1, heartRate);
            }

            // this updates min/max heart rates
            if (heartRate > maxHeartRate.getHeartRate()) {
                maxHeartRate.setHeartRate(heartRate);
                maxHeartRate.setIndex(heartRateDataList.size() - 1);
            }
            if (heartRate < minHeartRate.getHeartRate()) {
                minHeartRate.setHeartRate(heartRate);
                minHeartRate.setIndex(heartRateDataList.size() - 1);
            }
        }
    }


    public void completeSession() {
        completeSession(this.objectiveMet, this.productivityRating);
    }

    // Complete session logs all the session data
    public void completeSession(Boolean objectiveMet, int productivityRating) { // save the duration and end time
        endTime = LocalDateTime.now();
        if (startTime != null) {
            // save the session data
            duration = (int) Duration.between(startTime, endTime).getSeconds();
            this.objectiveMet = objectiveMet;

            // set productivity rating
            if(0 <= productivityRating && productivityRating <= 10) {
                this.productivityRating = productivityRating;   // user rating
            } else {
                this.productivityRating = NULL; // make sure to avoid using the empty value in averages
            }

            // calculate average heart rate
            if (!heartRateDataList.isEmpty()) {
                int sum = 0;
                for (int hr : heartRateDataList) {
                    sum += hr;
                }
                averageHeartRate = sum / heartRateDataList.size();
            }
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDuration() {
        return duration;
    }

    public Status getStatus() {
        return status;
    }
    public Boolean getObjectiveMet() {
        return objectiveMet;
    }
    public int getProductivityRating() {
        return productivityRating;
    }

    public int[] getHeartRateData() {
        int[] data = new int[heartRateDataList.size()];
        for (int i = 0; i < heartRateDataList.size(); i++) {
            data[i] = heartRateDataList.get(i);
        }
        return data;
    }

    public int getRestingHeartRate() {
        return restingHeartRate;
    }
    public int getHeartRate() {
        return averageHeartRate;
    }
    public int getMaxHeartRate() {
        return maxHeartRate != null ? maxHeartRate.getHeartRate() : 0;
    }
    public int getMinHeartRate() {
        return minHeartRate != null ? minHeartRate.getHeartRate() : 0;
    }
    public int getMaxHeartRateIndex() {
        return maxHeartRate != null ? maxHeartRate.getIndex() : -1;
    }
    public int getMinHeartRateIndex() {
        return minHeartRate != null ? minHeartRate.getIndex() : -1;
    }

    public void setCurrentHeartRateReading(HeartRateReading reading) {
        this.currentHeartRateReading = reading;
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
    public void setObjectiveMet(Boolean objectiveMet) {
        this.objectiveMet = objectiveMet;
    }
    public void setProductivityRating(int productivityRating) {
        this.productivityRating = productivityRating;
    }

    public void setRestingHeartRate(int restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }
    public void setHeartRate(int averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
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

    public void addHistoricalHeartRate(int hr) {
        this.heartRateDataList.add(hr);
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
