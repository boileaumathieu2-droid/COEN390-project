package com.example.zone.controller;

import com.example.zone.model.TimerModel;
import com.example.zone.view.TimerSettingsView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class TimerSettingsControllerTest {

    private TimerSettingsController controller;
    private TimerSettingsView activity;
    private TimerModel model;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // We use Robolectric to build the actual activity so findViewById works
        activity = Robolectric.buildActivity(TimerSettingsView.class).create().get();
        controller = new TimerSettingsController(activity);
        model = TimerModel.getInstance();
        model.stopAndReset();
    }

    @Test
    public void testInitializeViewLoadsCorrectData() {
        model.setStudyDuration(1800); // 30 mins
        model.setBreakDuration(600);  // 10 mins
        model.setBreakEnabled(true);

        controller.initializeView();

        assertEquals("30", activity.getStudyMinsText());
        assertEquals("0", activity.getStudySecsText());
        assertEquals("10", activity.getBreakMinsText());
        assertEquals("0", activity.getBreakSecsText());
        assertTrue(activity.isBreakTimerEnabled());
    }

    @Test
    public void testSaveSettingsUpdatesModel() {
        // Prepare view with new data
        activity.setStudyMins("20");
        activity.setStudySecs("30");
        activity.setBreakMins("5");
        activity.setBreakSecs("15");
        activity.setBreakEnabled(true);

        controller.saveSettings();

        assertEquals(20 * 60 + 30, model.getStudyDuration());
        assertEquals(5 * 60 + 15, model.getBreakDuration());
        assertTrue(model.isBreakEnabled());
    }

    @Test
    public void testSaveSettingsWithEmptyInputsUsesDefaults() {
        activity.setStudyMins("");
        activity.setStudySecs("");
        activity.setBreakMins("");
        activity.setBreakSecs("");

        controller.saveSettings();

        // Check defaults from parseOrDefault
        assertEquals(25 * 60 + 0, model.getStudyDuration());
        assertEquals(5 * 60 + 0, model.getBreakDuration());
    }
}
