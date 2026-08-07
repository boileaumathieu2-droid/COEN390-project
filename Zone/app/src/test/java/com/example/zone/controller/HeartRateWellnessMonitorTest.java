package com.example.zone.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.zone.model.HeartRateReading;

import org.junit.Test;

public class HeartRateWellnessMonitorTest {

    @Test
    public void warnsOnlyAfterTenContinuousSecondsAboveOneHundred() {
        HeartRateWellnessMonitor monitor = new HeartRateWellnessMonitor();

        assertNull(monitor.evaluate(reading(101), true, 1_000L));
        assertNull(monitor.evaluate(reading(106), true, 6_000L));
        assertNull(monitor.evaluate(reading(104), true, 10_999L));
        assertNotNull(monitor.evaluate(reading(104), true, 11_000L));
    }

    @Test
    public void normalReadingOrPausedSessionResetsTheTimer() {
        HeartRateWellnessMonitor monitor = new HeartRateWellnessMonitor();

        assertNull(monitor.evaluate(reading(110), true, 1_000L));
        assertNull(monitor.evaluate(reading(95), true, 6_000L));
        assertNull(monitor.evaluate(reading(110), true, 11_000L));
        assertNull(monitor.evaluate(reading(110), true, 16_000L));

        monitor.reset();
        assertNull(monitor.evaluate(reading(110), true, 1_000L));
        assertNull(monitor.evaluate(reading(110), false, 6_000L));
        assertNull(monitor.evaluate(reading(110), true, 11_000L));
        assertNull(monitor.evaluate(reading(110), true, 16_000L));
    }

    private HeartRateReading reading(int bpm) {
        return new HeartRateReading(600, 45, bpm, "OK");
    }
}
