//package com.example.zone.view;
//
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
//import static androidx.test.espresso.action.ViewActions.replaceText;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static org.hamcrest.Matchers.not;
//import static org.junit.Assert.assertEquals;
//
//import androidx.test.espresso.Espresso;
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import com.example.zone.R;
//import com.example.zone.model.TimerModel;
//
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(AndroidJUnit4.class)
//public class TimerSettingsViewTest {
//
//    @Rule
//    public ActivityScenarioRule<TimerSettingsView> activityRule =
//            new ActivityScenarioRule<>(TimerSettingsView.class);
//
//    @Before
//    public void setUp() {
//        TimerModel.getInstance().stopAndReset();
//    }
//
//    @Test
//    public void testLayoutElementsArePresent() {
//        Espresso.onView(withId(R.id.label_study_minutes))
//                .check(matches(isDisplayed()));
//
//        Espresso.onView(withId(R.id.edit_study_minutes))
//                .check(matches(isDisplayed()));
//
//        Espresso.onView(withId(R.id.edit_study_seconds))
//                .check(matches(isDisplayed()));
//
//        Espresso.onView(withId(R.id.switch_break_timer))
//                .check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void testBreakTimerToggleVisibility() {
//        Espresso.onView(withId(R.id.text_break_label))
//                .check(matches(not(isDisplayed())));
//
//        Espresso.onView(withId(R.id.switch_break_timer))
//                .perform(click());
//
//        Espresso.onView(withId(R.id.text_break_label))
//                .check(matches(isDisplayed()));
//
//        Espresso.onView(withId(R.id.edit_break_minutes))
//                .check(matches(isDisplayed()));
//
//        Espresso.onView(withId(R.id.switch_break_timer))
//                .perform(click());
//
//        Espresso.onView(withId(R.id.text_break_label))
//                .check(matches(not(isDisplayed())));
//    }
//
//    @Test
//    public void testSaveSettingsPersistence() {
//        Espresso.onView(withId(R.id.edit_study_minutes))
//                .perform(replaceText("15"), closeSoftKeyboard());
//
//        Espresso.onView(withId(R.id.edit_study_seconds))
//                .perform(replaceText("30"), closeSoftKeyboard());
//
//        Espresso.onView(withId(R.id.button_save_settings))
//                .perform(click());
//
//        TimerModel model = TimerModel.getInstance();
//
//        assertEquals(15 * 60 + 30, model.getStudyDuration());
//    }
//}