package com.example.zone.model;

/** One complete heart-rate packet received from the Bluno Nano. */
public final class HeartRateReading {
    private final int rawValue;
    private final int signalRange;
    private final int bpm;
    private final String status;

    public HeartRateReading(int rawValue, int signalRange, int bpm, String status) {
        this.rawValue = rawValue;
        this.signalRange = signalRange;
        this.bpm = bpm;
        this.status = status;
    }

    public int getRawValue() {
        return rawValue;
    }

    public int getSignalRange() {
        return signalRange;
    }

    public int getBpm() {
        return bpm;
    }

    public String getStatus() {
        return status;
    }

    public boolean hasGoodSignal() {
        return "OK".equalsIgnoreCase(status);
    }

    public String toPacketString() {
        return "HR," + rawValue + "," + signalRange + "," + bpm + "," + status;
    }
}
