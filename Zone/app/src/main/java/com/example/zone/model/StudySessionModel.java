package com.example.zone.model;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String documentId;
    private int duration;
    private Status status;
    private int restingHeartRate;   // take heart rate value at time 0
    private Boolean objectiveMet = false;   // default, rating at session end
    private int productivityRating; // rated on session completion, -1 = not rated
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
        productivityRating = -1; // -1 represents "not rated"

    }
    public static StudySessionModel getInstance() {

        if(instance == null) {
            instance = new StudySessionModel();
        }
        return instance;
    }

    public static void reset() {
        instance = new StudySessionModel();
    }
    // function that gets the current heart rate value from HeartRateMonitorView.java and HeartRateReading.java
    public synchronized int getHeartRateReading() {
        if (currentHeartRateReading != null && currentHeartRateReading.hasGoodSignal()) {
            return currentHeartRateReading.getBpm();
        }
        return 0;
    }
    public synchronized void startSession() {
        // Start with clean analytics while retaining the most recent live sensor
        // packet so the first sample can be recorded immediately.
        endTime = null;
        duration = 0;
        objectiveMet = false;
        productivityRating = -1;
        heartRateDataList.clear();
        averageHeartRate = 0;
        maxHeartRate = null;
        minHeartRate = null;
        startTime = LocalDateTime.now();
        status = Status.ACTIVE;
        restingHeartRate = getHeartRateReading();
        addHeartRateReading();
    }
    public synchronized void addHeartRateReading(){
        int heartRate = getHeartRateReading();
        if (heartRate > 0) {
            if (restingHeartRate <= 0) {
                restingHeartRate = heartRate;
            }
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


    public synchronized void completeSession() {
        completeSession(this.objectiveMet, this.productivityRating);
    }

    // Complete session logs all the session data
    public synchronized void completeSession(Boolean objectiveMet, int productivityRating) { // save the duration and end time
        endTime = LocalDateTime.now();
        if (startTime != null) {
            // save the session data
            duration = (int) Duration.between(startTime, endTime).getSeconds();
            this.objectiveMet = objectiveMet;

            // set productivity rating
            if(0 <= productivityRating && productivityRating <= 10) {
                this.productivityRating = productivityRating;   // user rating
            } else {
                this.productivityRating = -1; // -1 represents "not rated"
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

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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

    public synchronized int[] getHeartRateData() {
        int[] data = new int[heartRateDataList.size()];
        for (int i = 0; i < heartRateDataList.size(); i++) {
            data[i] = heartRateDataList.get(i);
        }
        return data;
    }
    public synchronized List<Integer> getHeartRateDataList() {
        return new ArrayList<>(heartRateDataList);
    }

    public synchronized int getRestingHeartRate() {
        return restingHeartRate;
    }
    public synchronized int getHeartRate() {
        return averageHeartRate;
    }
    public synchronized int getMaxHeartRate() {
        return maxHeartRate != null ? maxHeartRate.getHeartRate() : 0;
    }
    public synchronized int getMinHeartRate() {
        return minHeartRate != null ? minHeartRate.getHeartRate() : 0;
    }
    public int getMaxHeartRateIndex() {
        return maxHeartRate != null ? maxHeartRate.getIndex() : -1;
    }
    public int getMinHeartRateIndex() {
        return minHeartRate != null ? minHeartRate.getIndex() : -1;
    }

    public synchronized void setCurrentHeartRateReading(HeartRateReading reading) {
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
        if (this.maxHeartRate == null) {
            this.maxHeartRate = new HeartRateInstance(-1, maxHeartRate);
        } else {
            this.maxHeartRate.setHeartRate(maxHeartRate);
        }
    }
    public void setMinHeartRate(int minHeartRate) {
        if (this.minHeartRate == null) {
            this.minHeartRate = new HeartRateInstance(-1, minHeartRate);
        } else {
            this.minHeartRate.setHeartRate(minHeartRate);
        }
    }
    public void setMaxHeartRateIndex(int maxHeartRateIndex) {
        if (this.maxHeartRate == null) {
            this.maxHeartRate = new HeartRateInstance(maxHeartRateIndex, 0);
        } else {
            this.maxHeartRate.setIndex(maxHeartRateIndex);
        }
    }
    public void setMinHeartRateIndex(int minHeartRateIndex) {
        if (this.minHeartRate == null) {
            this.minHeartRate = new HeartRateInstance(minHeartRateIndex, 0);
        } else {
            this.minHeartRate.setIndex(minHeartRateIndex);
        }
    }
    public synchronized void addHistoricalHeartRate(int hr) {
        this.heartRateDataList.add(hr);
    }

    public boolean isActive() { // when the session is running
        return status == Status.ACTIVE;
    }

    public void endSession(int duration) {  // at the end of the session, this triggers
        if (status != Status.ACTIVE || duration < 0) {
            return;
        }

        this.duration = duration;
        this.endTime = LocalDateTime.now();
        calculateAverageHeartRate();
        this.status = Status.COMPLETE;
    }

    private void calculateAverageHeartRate() {
        if (heartRateDataList.isEmpty()) {
            averageHeartRate = 0;
            return;
        }
        long total = 0;
        for (int heartRate : heartRateDataList) {
            total += heartRate;
        }
        averageHeartRate = (int) (total / heartRateDataList.size());
    }
    public synchronized Map<String, Object> toMap() {

        Map<String, Object> session = new HashMap<>();

        session.put("startTime", startTime != null ? startTime.toString() : null);
        session.put("endTime", endTime != null ? endTime.toString() : null);
        session.put("duration", duration);
        session.put("status", status.toString());

        session.put("restingHeartRate", restingHeartRate);
        session.put("objectiveMet", objectiveMet);
        session.put("productivityRating", productivityRating);

        session.put("heartRateData", new ArrayList<>(heartRateDataList));
        session.put("averageHeartRate", averageHeartRate);

        session.put("maxHeartRate", maxHeartRate != null ?
                maxHeartRate.getHeartRate() : 0);

        session.put("minHeartRate", minHeartRate != null ?
                minHeartRate.getHeartRate() : 0);

        return session;
    }
}
