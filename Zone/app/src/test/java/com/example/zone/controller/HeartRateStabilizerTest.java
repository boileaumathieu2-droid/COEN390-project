package com.example.zone.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.example.zone.model.HeartRateReading;

import org.junit.Test;

public class HeartRateStabilizerTest {

    @Test
    public void passesGoodReadingThroughUnchanged() {
        HeartRateStabilizer stabilizer = new HeartRateStabilizer();
        HeartRateReading input = reading(143);
        HeartRateReading output = stabilizer.filter(input);
        assertEquals(143, output.getBpm());
        assertEquals(input, output);
    }

    @Test
    public void passesNoSignalThroughUnchanged() {
        HeartRateStabilizer stabilizer = new HeartRateStabilizer();
        HeartRateReading input = new HeartRateReading(195, 6, 0, "NO_SIGNAL");
        HeartRateReading output = stabilizer.filter(input);
        assertFalse(output.hasGoodSignal());
        assertEquals(0, output.getBpm());
        assertEquals(input, output);
    }

    private HeartRateReading reading(int bpm) {
        return new HeartRateReading(600, 45, bpm, "OK");
    }
}
