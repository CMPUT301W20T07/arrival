package com.example.android.arrival;

import android.app.Activity;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.android.arrival.Activities.RequestTestActivity;
import com.example.android.arrival.Model.Place;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Util.RequestCallbackListener;
import com.example.android.arrival.Util.RequestManager;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Provides testing of the RequestManager to verify correct
 * additions, deletions, updates and callbacks from the
 * Firebase FireStore Cloud Database.
 */
public class RequestManagerTest {

    private Solo solo;

    private Request mockRequest() {
        Place pickup = new Place("Henday Hall", "6969 Big Hammer Ln.", 69.69, 420.1337);
        Place destination = new Place("Panda Express", "1985 Loser Dr.", 17.38, 419.999);
        return new Request("rm-test-user", pickup, destination, 1.0f);
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

    /**
     * Test opening a Request, updating a list of current requests,
     * deleting a request.
     */
    @Test
    public void testOpenRequest() {
        solo.assertCurrentActivity("Wrong Activity", RequestTestActivity.class);

        RequestManager rm = RequestManager.getInstance();

        // Open request
        Request req = mockRequest();
        rm.openRequest(req, (RequestCallbackListener) solo.getCurrentActivity());
        assertTrue(solo.waitForText(req.getID(), 1, 5000)); // Wait for entry

        // Delete request
        rm.deleteRequest(req.getID(), (RequestCallbackListener) solo.getCurrentActivity());
        //assertTrue(!solo.waitForText(req.getID(), 1, 10000)); // Wait for entry
    }


    /**
     * Test opening a Request, updating a list of currently open requests
     * updating the request, and the deleting the request.
     */
    @Test
    public void testUpdateRequest() {
        solo.assertCurrentActivity("Wrong Activity", RequestTestActivity.class);

        RequestManager rm = RequestManager.getInstance();

        // Open request
        Request req = mockRequest();
        rm.openRequest(req, (RequestCallbackListener) solo.getCurrentActivity());
        assertTrue(solo.waitForText("rm-test-user", 1, 5000)); // Wait for entry

        // Update request
        req.setDriver("test-driver");
        rm.updateRequest(req, (RequestCallbackListener) solo.getCurrentActivity());
        assertTrue(solo.waitForText("test-driver", 1, 10000)); // Wait for entry

        // Delete request
        rm.deleteRequest(req.getID(), (RequestCallbackListener) solo.getCurrentActivity());
        //assertTrue(!solo.waitForText(req.getID(), 1, 10000)); // Wait for entry
    }
}
