package com.example.zone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.zone.model.StudySessionModel;

import org.junit.Test;

public class testStudySessionModel {
    StudySessionModel model = new StudySessionModel();

    @Test
    public void testStartSession() {
        model.startSession();

        assertEquals(StudySessionModel.Status.ACTIVE, model.getStatus());
    }
    @Test
    public void testEndSession() {
        model.startSession();
        int duration =4700;
        model.endSession(duration);
        assertEquals(StudySessionModel.Status.COMPLETE, model.getStatus());
    }
    @Test
    public void testisActive() {
        model.startSession();
        assertTrue(model.isActive());
    }
}

