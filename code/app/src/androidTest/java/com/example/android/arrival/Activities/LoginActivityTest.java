package com.example.android.arrival.Activities;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.test.rule.ActivityTestRule;

import com.example.android.arrival.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.TestCase.assertNotNull;

// Youtube video by Learn Share Anything Anyone (https://www.youtube.com/channel/UClq3QxwbzVVoTdyccHla0-g)
// "Android app development for beginners - 26 - Android - Unit test for Activity - Activity Test Rule"
// https://www.youtube.com/watch?v=_TR6QcRozAg
public class LoginActivityTest {
    @Rule
    public ActivityTestRule<LoginActivity> lActivityTestRule = new ActivityTestRule<>(LoginActivity.class, true, true);
    private LoginActivity lActivity = null;

    Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(RegistrationActivity.class.getName(), null, false);

    /**
     * Runs before all tests
     * Gets the login activity
     * @throws Exception
     */

    @Before
    public void setUp() throws Exception {
        lActivity = lActivityTestRule.getActivity();
    }

    /**
     * Testing that Registration activity is launched when the signUp button is clicked
     */
    @Test
    public void testLaunchofRegistrationActivityOnButtonClick() {
        assertNotNull(lActivity.findViewById(R.id.sign_up_button));

        onView(withId(R.id.sign_up_button)).perform(click());

        Activity RegistrationActivity = getInstrumentation().waitForMonitorWithTimeout(monitor, 5000);
        assertNotNull(RegistrationActivity);

        RegistrationActivity.finish();
    }


    @After
    public void tearDown() throws Exception {
        lActivity = null;
    }

}