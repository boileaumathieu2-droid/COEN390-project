package com.example.zone;

import com.example.zone.model.StudySessionModel;
import static org.junit.Assert.*;
import org.junit.Test;

import java.time.LocalDateTime;

public class StudySessionTest {

    @Test
    public void testSessionStart() {
        StudySessionModel session = new StudySessionModel();

        session.startSession();

        assertEquals(StudySessionModel.Status.ACTIVE, session.getStatus());
    }

    @Test
    public void testEndSessionValidDuration() {
        StudySessionModel session = new StudySessionModel();

        session.startSession();

        int duration = 2700; // about 45 minutes
        session.endSession(duration);

        assertEquals(StudySessionModel.Status.COMPLETE, session.getStatus());
        assertEquals(duration, session.getDuration());
        assertTrue(session.getEndTime() == LocalDateTime.now());
    }

    @Test
    public void testEndSessionNegativeDuration() {
        StudySessionModel session = new StudySessionModel();

        session.startSession();

        int duration = -100;

        session.endSession(duration);

        // Invalid duration should not complete the session
        assertEquals(StudySessionModel.Status.ACTIVE, session.getStatus());
    }

    @Test
    public void testConstructor() {
        StudySessionModel session = new StudySessionModel();

        assertEquals(StudySessionModel.Status.INACTIVE, session.getStatus());
    }

    @Test
    public void testEndSessionWithoutStarting() {
        StudySessionModel session = new StudySessionModel();

        int duration = 2700;

        session.endSession(duration);

        // Cannot complete a session that was never started
        assertEquals(StudySessionModel.Status.INACTIVE, session.getStatus());
    }
}
