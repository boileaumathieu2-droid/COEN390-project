package com.example.zone.controller;

import com.example.zone.model.HeartRateReading;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reassembles BLE chunks into Arduino lines and parses:
 * HR,raw,signalRange,bpm,status
 */
public final class HeartRatePacketParser {
    private static final int MAX_BUFFER_LENGTH = 4096;
    private final StringBuilder pendingText = new StringBuilder();

    public synchronized List<HeartRateReading> append(byte[] bytes) {
        List<HeartRateReading> readings = new ArrayList<>();
        if (bytes == null || bytes.length == 0) {
            return readings;
        }

        pendingText.append(new String(bytes, StandardCharsets.UTF_8));

        int newlineIndex;
        while ((newlineIndex = pendingText.indexOf("\n")) >= 0) {
            String line = pendingText.substring(0, newlineIndex).replace("\r", "").trim();
            pendingText.delete(0, newlineIndex + 1);

            HeartRateReading reading = parseLine(line);
            if (reading != null) {
                readings.add(reading);
            }
        }

        // Do not retain an unlimited amount of malformed/no-newline input.
        if (pendingText.length() > MAX_BUFFER_LENGTH) {
            pendingText.delete(0, pendingText.length() - MAX_BUFFER_LENGTH);
        }
        return readings;
    }

    public synchronized void reset() {
        pendingText.setLength(0);
    }

    public static HeartRateReading parseLine(String line) {
        if (line == null) {
            return null;
        }

        String[] parts = line.trim().split(",");
        if (parts.length != 5 || !"HR".equalsIgnoreCase(parts[0].trim())) {
            return null;
        }

        try {
            int rawValue = Integer.parseInt(parts[1].trim());
            int signalRange = Integer.parseInt(parts[2].trim());
            int bpm = Integer.parseInt(parts[3].trim());
            String status = parts[4].trim().toUpperCase();

            if (rawValue < 0 || rawValue > 1023
                    || signalRange < 0 || signalRange > 1023
                    || bpm < 0 || bpm > 250
                    || !("OK".equals(status) || "NO_SIGNAL".equals(status))) {
                return null;
            }

            return new HeartRateReading(rawValue, signalRange, bpm, status);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
