package com.example.zone.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;
import java.util.Map;

public class StudySessionHeartRateTest {

    @Test
    public void savesOnlyGoodReadingsWithReflectionFields() {
        StudySessionModel session = new StudySessionModel();
        session.startSession();

        session.setCurrentHeartRateReading(
                new HeartRateReading(195, 6, 0, "NO_SIGNAL")
        );
        session.addHeartRateReading();
        assertEquals(0, session.getHeartRateData().length);

        session.setCurrentHeartRateReading(
                new HeartRateReading(610, 44, 76, "OK")
        );
        session.addHeartRateReading();
        session.setCurrentHeartRateReading(
                new HeartRateReading(615, 46, 80, "OK")
        );
        session.addHeartRateReading();
        session.completeSession(true, 8);

        Map<String, Object> saved = session.toMap();
        assertEquals(Boolean.TRUE, saved.get("objectiveMet"));
        assertEquals(8, saved.get("productivityRating"));
        assertEquals(76, saved.get("restingHeartRate"));
        assertEquals(80, saved.get("maxHeartRate"));
        assertEquals(76, saved.get("minHeartRate"));
        assertEquals(2, ((List<?>) saved.get("heartRateData")).size());
        assertTrue((Integer) saved.get("averageHeartRate") > 0);
    }
}
