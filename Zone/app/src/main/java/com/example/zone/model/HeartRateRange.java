package com.example.zone.model;

public final class HeartRateRange {

    public enum Level {
        NO_READING,
        TYPICAL,
        CAUTION,
        ALERT
    }

    private HeartRateRange() {
    }

    public static Level classify(int bpm) {
        if (bpm <= 0) {
            return Level.NO_READING;
        }
        if (bpm >= 60 && bpm <= 100) {
            return Level.TYPICAL;
        }
        if ((bpm >= 50 && bpm < 60) || (bpm > 100 && bpm <= 120)) {
            return Level.CAUTION;
        }
        return Level.ALERT;
    }
}
