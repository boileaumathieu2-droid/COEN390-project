package com.example.zone.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.zone.model.HeartRateReading;

import org.junit.Test;

public class HeartRateStabilizerTest {

    @Test
    public void ignoresNoSignalAndWaitsForEnoughGoodSamples() {
        HeartRateStabilizer stabilizer = new HeartRateStabilizer();

        HeartRateReading noSignal = stabilizer.filter(
                new HeartRateReading(195, 6, 0, "NO_SIGNAL")
        );
        assertFalse(noSignal.hasGoodSignal());
        assertEquals(0, noSignal.getBpm());

        assertEquals(0, stabilizer.filter(reading(78)).getBpm());
        assertEquals(0, stabilizer.filter(reading(79)).getBpm());
        assertEquals(0, stabilizer.filter(reading(80)).getBpm());
        assertEquals(0, stabilizer.filter(reading(79)).getBpm());
        assertTrue(stabilizer.filter(reading(80)).getBpm() > 0);
    }

    @Test
    public void aSingleSpikeDoesNotReplaceTheStableReading() {
        HeartRateStabilizer stabilizer = new HeartRateStabilizer();
        stabilizer.filter(reading(78));
        stabilizer.filter(reading(79));
        stabilizer.filter(reading(80));
        stabilizer.filter(reading(79));
        int stable = stabilizer.filter(reading(80)).getBpm();

        int afterSpike = stabilizer.filter(reading(150)).getBpm();
        assertEquals(stable, afterSpike);
    }

    private HeartRateReading reading(int bpm) {
        return new HeartRateReading(600, 45, bpm, "OK");
    }
}
