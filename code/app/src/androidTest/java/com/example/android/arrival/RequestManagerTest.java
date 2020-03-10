package com.example.android.arrival;

import android.app.Activity;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.android.arrival.Activities.RequestTestActivity;
import com.example.android.arrival.Model.GeoLocation;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.RequestCallbackListener;
import com.example.android.arrival.Model.RequestManager;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertTrue;

public class RequestManagerTest {

    private Solo solo;

    private Request mockRequest() {
        return new Request("rm-test-user", new GeoLocation(), new GeoLocation(), 1.0f);
    }

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
    public void testOpenRequest() {
        solo.assertCurrentActivity("Wrong Activity", RequestTestActivity.class);

        RequestManager rm = RequestManager.getInstance();

        Request req = mockRequest();
        rm.openRequest(req, (RequestCallbackListener) solo.getCurrentActivity());

        assertTrue(solo.waitForText("rm-test-user", 1, 5000)); // Wait for entry

        rm.deleteRequest(req.getID(), (RequestCallbackListener) solo.getCurrentActivity());

        assertTrue(!solo.waitForText("rm-test-user", 1, 2000)); // Wait for entry
    }
}
