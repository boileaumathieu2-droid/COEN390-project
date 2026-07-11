package com.example.zone.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TimerModelTest {

    private TimerModel timerModel;

    @Before
    public void setUp() {
        timerModel = TimerModel.getInstance();
        // Reset to default state before each test
        timerModel.stopAndReset();
        timerModel.setStudyDuration(1500); // 25 min
        timerModel.setBreakDuration(300);  // 5 min
        timerModel.setBreakEnabled(false);
    }

    @Test
    public void testInitialState() {
        assertEquals(1500, timerModel.getStudyDuration());
        assertEquals(1500, timerModel.getMinutes() * 60 + timerModel.getSeconds());
        assertFalse(timerModel.isRunning());
        assertFalse(timerModel.isBreakTime());
    }

    @Test
    public void testStartPauseTimer() {
        timerModel.startTimer();
        assertTrue(timerModel.isRunning());
        
        timerModel.pauseTimer();
        assertFalse(timerModel.isRunning());
    }

    @Test
    public void testTickReducesTime() {
        timerModel.startTimer();
        int initialTime = timerModel.getMinutes() * 60 + timerModel.getSeconds();
        timerModel.tick();
        int afterTickTime = timerModel.getMinutes() * 60 + timerModel.getSeconds();
        assertEquals(initialTime - 1, afterTickTime);
    }

    @Test
    public void testTransitionToBreakWhenEnabled() {
        timerModel.setBreakEnabled(true);
        timerModel.setStudyDuration(10); // 10 seconds for quick test
        timerModel.stopAndReset();
        timerModel.startTimer();
        
        // Manually tick down to 0
        for (int i = 0; i < 10; i++) {
            timerModel.tick();
        }
        
        // At 0, it should stop and switch to break
        assertFalse(timerModel.isRunning());
        assertTrue(timerModel.isBreakTime());
        assertEquals(300, timerModel.getMinutes() * 60 + timerModel.getSeconds());
    }

    @Test
    public void testTransitionToStudyAfterBreak() {
        timerModel.setBreakEnabled(true);
        timerModel.setStudyDuration(1500);
        timerModel.setBreakDuration(10);
        
        // Manually trigger break state
        timerModel.completeSession(); 
        assertTrue(timerModel.isBreakTime());
        
        timerModel.startTimer();
        for (int i = 0; i < 10; i++) {
            timerModel.tick();
        }
        
        // Break finished, should be back to study state
        assertFalse(timerModel.isBreakTime());
        assertEquals(1500, timerModel.getMinutes() * 60 + timerModel.getSeconds());
    }

    @Test
    public void testCompleteSessionManual() {
        timerModel.setBreakEnabled(true);
        timerModel.completeSession();
        
        assertTrue(timerModel.isBreakTime());
        assertFalse(timerModel.isRunning());
    }
}
