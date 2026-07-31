package com.example.zone.controller;

import com.example.zone.model.HeartRateReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Removes short BPM spikes before readings are displayed or saved.
 * Raw sensor and signal-range values are left unchanged for diagnostics.
 */
public final class HeartRateStabilizer {
    private static final int MIN_VALID_BPM = 35;
    private static final int MAX_VALID_BPM = 220;
    private static final int WINDOW_SIZE = 5;
    private static final int REQUIRED_SAMPLES = 3;
    private static final int MAX_CHANGE_PER_UPDATE = 5;

    private final List<Integer> recentBpm = new ArrayList<>();
    private Integer stableBpm;

    public synchronized HeartRateReading filter(HeartRateReading reading) {
        if (reading == null) {
            return null;
        }

        int bpm = reading.getBpm();
        if (!reading.hasGoodSignal() || bpm < MIN_VALID_BPM || bpm > MAX_VALID_BPM) {
            return new HeartRateReading(
                    reading.getRawValue(),
                    reading.getSignalRange(),
                    0,
                    reading.getStatus());
        }

        recentBpm.add(bpm);
        if (recentBpm.size() > WINDOW_SIZE) {
            recentBpm.remove(0);
        }

        if (recentBpm.size() < REQUIRED_SAMPLES) {
            return new HeartRateReading(
                    reading.getRawValue(),
                    reading.getSignalRange(),
                    0,
                    reading.getStatus());
        }

        List<Integer> sorted = new ArrayList<>(recentBpm);
        Collections.sort(sorted);
        int median = sorted.get(sorted.size() / 2);

        if (stableBpm == null) {
            stableBpm = median;
        } else {
            int smoothed = Math.round(stableBpm * 0.70f + median * 0.30f);
            int difference = smoothed - stableBpm;
            if (difference > MAX_CHANGE_PER_UPDATE) {
                smoothed = stableBpm + MAX_CHANGE_PER_UPDATE;
            } else if (difference < -MAX_CHANGE_PER_UPDATE) {
                smoothed = stableBpm - MAX_CHANGE_PER_UPDATE;
            }
            stableBpm = smoothed;
        }

        return new HeartRateReading(
                reading.getRawValue(),
                reading.getSignalRange(),
                stableBpm,
                reading.getStatus());
    }

    public synchronized void reset() {
        recentBpm.clear();
        stableBpm = null;
    }
}
