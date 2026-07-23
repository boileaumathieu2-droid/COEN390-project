package com.example.zone.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.zone.model.HeartRateReading;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class HeartRatePacketParserTest {
    @Test
    public void parsesCompletePacket() {
        HeartRateReading reading = HeartRatePacketParser.parseLine("HR,612,48,78,OK");

        assertNotNull(reading);
        assertEquals(612, reading.getRawValue());
        assertEquals(48, reading.getSignalRange());
        assertEquals(78, reading.getBpm());
        assertTrue(reading.hasGoodSignal());
    }

    @Test
    public void reassemblesPacketSplitAcrossBleNotifications() {
        HeartRatePacketParser parser = new HeartRatePacketParser();
        assertTrue(parser.append("HR,612,48,".getBytes(StandardCharsets.UTF_8)).isEmpty());

        List<HeartRateReading> readings = parser.append(
                "78,OK\r\nHR,196,6,0,NO_SIGNAL\n".getBytes(StandardCharsets.UTF_8));

        assertEquals(2, readings.size());
        assertEquals(78, readings.get(0).getBpm());
        assertFalse(readings.get(1).hasGoodSignal());
    }

    @Test
    public void rejectsMalformedOrUnsafeValues() {
        assertNull(HeartRatePacketParser.parseLine("not a packet"));
        assertNull(HeartRatePacketParser.parseLine("HR,612,48,300,OK"));
        assertNull(HeartRatePacketParser.parseLine("HR,612,48,78,UNKNOWN"));
    }
}
