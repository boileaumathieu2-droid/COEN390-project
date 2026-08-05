package com.example.zone.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HeartRateRangeTest {

    @Test
    public void classifiesTypicalRestingReadings() {
        assertEquals(HeartRateRange.Level.TYPICAL, HeartRateRange.classify(60));
        assertEquals(HeartRateRange.Level.TYPICAL, HeartRateRange.classify(82));
        assertEquals(HeartRateRange.Level.TYPICAL, HeartRateRange.classify(100));
    }

    @Test
    public void classifiesCautionAndAlertReadings() {
        assertEquals(HeartRateRange.Level.CAUTION, HeartRateRange.classify(59));
        assertEquals(HeartRateRange.Level.CAUTION, HeartRateRange.classify(110));
        assertEquals(HeartRateRange.Level.ALERT, HeartRateRange.classify(49));
        assertEquals(HeartRateRange.Level.ALERT, HeartRateRange.classify(121));
        assertEquals(HeartRateRange.Level.NO_READING, HeartRateRange.classify(0));
    }
}
