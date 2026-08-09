package com.example.zone.controller;

import com.example.zone.model.HeartRateReading;

/** Passes each sensor packet through without smoothing or delaying it. */
public final class HeartRateStabilizer {

    public synchronized HeartRateReading filter(HeartRateReading reading) {
        return reading;
    }

    public synchronized void reset() {
        // No state is kept when readings are passed through unchanged.
    }
}
