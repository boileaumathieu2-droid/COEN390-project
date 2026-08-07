package com.example.zone.controller;

import com.example.zone.model.HeartRateReading;

/** Detects a sustained elevated reading without treating it as a diagnosis. */
public final class HeartRateWellnessMonitor {

    static final int ELEVATED_THRESHOLD_BPM = 100;
    static final long REQUIRED_DURATION_MS = 30_000L;
    private static final long MAX_SAMPLE_GAP_MS = 5_000L;
    private static final long REPEAT_INTERVAL_MS = 5 * 60_000L;

    private static final String[] SUGGESTIONS = {
            "Your heart rate has stayed elevated. Consider taking a short break and breathing slowly.",
            "Your heart rate is still above the usual resting range. A drink of water and a quiet pause may help.",
            "Consider standing up, stretching, or taking a short walk before continuing your study session."
    };

    private long elevatedSince = -1L;
    private long lastSampleTime = -1L;
    private long lastSuggestionTime = -1L;
    private int nextSuggestion;

    public synchronized String evaluate(
            HeartRateReading reading,
            boolean studySessionActive,
            long elapsedRealtimeMs
    ) {
        if (!studySessionActive
                || reading == null
                || !reading.hasGoodSignal()
                || reading.getBpm() <= ELEVATED_THRESHOLD_BPM) {
            resetElevatedPeriod();
            lastSampleTime = elapsedRealtimeMs;
            return null;
        }

        if (lastSampleTime >= 0L
                && elapsedRealtimeMs - lastSampleTime > MAX_SAMPLE_GAP_MS) {
            elevatedSince = elapsedRealtimeMs;
        } else if (elevatedSince < 0L) {
            elevatedSince = elapsedRealtimeMs;
        }
        lastSampleTime = elapsedRealtimeMs;

        if (elapsedRealtimeMs - elevatedSince < REQUIRED_DURATION_MS) {
            return null;
        }
        if (lastSuggestionTime >= 0L
                && elapsedRealtimeMs - lastSuggestionTime < REPEAT_INTERVAL_MS) {
            return null;
        }

        String suggestion = SUGGESTIONS[nextSuggestion % SUGGESTIONS.length];
        nextSuggestion++;
        lastSuggestionTime = elapsedRealtimeMs;
        return suggestion;
    }

    public synchronized void reset() {
        elevatedSince = -1L;
        lastSampleTime = -1L;
        lastSuggestionTime = -1L;
        nextSuggestion = 0;
    }

    private void resetElevatedPeriod() {
        elevatedSince = -1L;
    }
}
