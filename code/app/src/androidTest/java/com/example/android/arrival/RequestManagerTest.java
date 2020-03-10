package com.example.android.arrival;

import android.app.Activity;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.android.arrival.Activities.RequestTestActivity;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

public class RequestManagerTest {

    private Solo solo;

    @Rule
    public ActivityTestRule<RequestTestActivity> rule =
            new ActivityTestRule<>(RequestTestActivity.class, true, true);


    @Before
    public void setUp() {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    @Test
    public void start() throws Exception {
        Activity activity = rule.getActivity();
    }

    @Test
    public void openReq() {
        solo.assertCurrentActivity("Wrong Activity", RequestTestActivity.class);



    }

}
