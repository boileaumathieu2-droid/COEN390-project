package com.example.zone.view;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import com.example.zone.R;
import com.example.zone.model.TimerModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.*;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MainViewTimerTest {

    @Rule
    public ActivityScenarioRule<MainView> activityRule =
            new ActivityScenarioRule<>(MainView.class);

    @Before
    public void setUp() {
        TimerModel model = TimerModel.getInstance();
        model.stopAndReset();
        model.setStudyDuration(1500); // Reset to default 25min
    }

    @Test
    public void testTimerInitialDisplay() {
        Espresso.onView(withId(R.id.timerDisplay)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.timerDisplay)).check(matches(withText("25:00")));
        Espresso.onView(withId(R.id.timerTitle)).check(matches(withText("Time for Study")));
    }

    @Test
    public void testStartButtonStartsCountdown() throws InterruptedException {
        Espresso.onView(withId(R.id.startStudySeshButton)).perform(click());
        
        // Wait a bit for tick
        Thread.sleep(1100);
        
        // Should show 24:59 or less
        Espresso.onView(withId(R.id.timerDisplay)).check(matches(not(withText("25:00"))));
        assertTrue(TimerModel.getInstance().isRunning());
    }

    @Test
    public void testPauseResumeCycle() {
        // Start
        Espresso.onView(withId(R.id.startStudySeshButton)).perform(click());
        
        // Pause
        Espresso.onView(withId(R.id.pauseTimer)).perform(click());
        Espresso.onView(withId(R.id.pauseTimer)).check(matches(withText("Resume")));
        
        // Resume
        Espresso.onView(withId(R.id.pauseTimer)).perform(click());
        Espresso.onView(withId(R.id.pauseTimer)).check(matches(withText("Pause")));
    }

    @Test
    public void testResetButton() {
        // Start and then reset
        Espresso.onView(withId(R.id.startStudySeshButton)).perform(click());
        Espresso.onView(withId(R.id.resetTimer)).perform(click());
        
        Espresso.onView(withId(R.id.timerDisplay)).check(matches(withText("25:00")));
        Espresso.onView(withId(R.id.startStudySeshButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testCompleteButtonTransitionsToBreak() {
        // Enable break in model
        TimerModel.getInstance().setBreakEnabled(true);
        TimerModel.getInstance().setBreakDuration(300); // 5 min
        
        // Start
        Espresso.onView(withId(R.id.startStudySeshButton)).perform(click());
        
        // Complete
        Espresso.onView(withId(R.id.completeTimer)).perform(click());
        
        // Should show break time
        Espresso.onView(withId(R.id.timerTitle)).check(matches(withText("Break Time")));
        Espresso.onView(withId(R.id.timerDisplay)).check(matches(withText("05:00")));
    }
}
