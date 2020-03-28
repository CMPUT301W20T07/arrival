package com.example.android.arrival;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.android.arrival.Activities.RiderMapActivity;
import com.google.protobuf.StringValue;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotSame;
import static junit.framework.TestCase.assertTrue;


/**
 * Testing the buttons and views in the RiderMapActivtity and associate Fragments and Popups
 */
public class RiderMapActivityTest {
    private Solo solo;

    @Rule
    public ActivityTestRule<RiderMapActivity> rule =
            new ActivityTestRule<>(RiderMapActivity.class, true, true);


    /**
     * Runs before all tests and creates solo instance
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception{
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }


    /**
     * Gets the activity
     * @throws Exception
     */
    @Test
    public void start() throws Exception{
        Activity activity = rule.getActivity();
    }


    /**
     * Testing that by default the start location is automatically set
     */
    @Test
    public void testUserLocation() throws InterruptedException {
        //Asserts that we are in the right activity
        solo.assertCurrentActivity("Wrong activity", RiderMapActivity.class);
        RiderMapActivity activity = (RiderMapActivity) solo.getCurrentActivity();

        solo.sleep(10000);

        TextView start = (TextView) solo.getView(R.id.riderStartLocation);
        assertTrue(start.getText().length() > 0);
    }

    /**
     * Testing that clicking on the map and adding markers changes the TextViews
     * Also testing that clicking the back button on the popup does not add or change a location
     */
    @Test
    public void testClickingOnMap() {
        solo.assertCurrentActivity("Wrong activity", RiderMapActivity.class);
        RiderMapActivity activity = (RiderMapActivity) solo.getCurrentActivity();
        TextView start = (TextView) solo.getView(R.id.riderStartLocation);
        TextView end = (TextView) solo.getView(R.id.riderEndLocation);

        //Waits for the map to load
        solo.sleep(10000);

        //Gets the current location marker address
        String start1 = start.getText().toString();

        //Changes the pickup location
        solo.clickOnScreen(60, 120);
        solo.clickOnButton("Pickup");
        solo.sleep(2000);
        String start2 = start.getText().toString();

        //Making sure the address in the textview changed
        assertTrue(start.getText().length() > 0);
        assertNotSame(start1, start2);

        solo.sleep(2000);

        //Adds dest to the map and asserts that the textview now has text in it
        solo.clickOnScreen(500, 300);
        solo.clickOnButton("Destination");
        solo.sleep(2000);
        assertTrue(end.getText().length() > 0);
        String end1 = end.getText().toString();
        solo.sleep(2000);

        //Changing the destination marker
        solo.clickOnScreen(200, 400);
        solo.clickOnButton("Destination");
        solo.sleep(2000);
        assertTrue(end.getText().length() > 0);
        String end2 = end.getText().toString();
        assertNotSame(end1, end2);

        solo.sleep(2000);
        solo.clickOnScreen(250, 125);
        solo.clickOnButton("Back");
        solo.sleep(2000);
        assertEquals(end2, end.getText().toString());
        assertEquals(start2, start.getText().toString());
    }


    /**
     * Testing that clicking the TextViews opens the SearchFragment
     * Testing that not selecting anything in the SearchView doesn't affect the current markers
     */
    @Test
    public void testOpenSearchFragments() {
        solo.assertCurrentActivity("Wrong activity", RiderMapActivity.class);
        RiderMapActivity activity = (RiderMapActivity) solo.getCurrentActivity();

        solo.sleep(10000);

        TextView start = (TextView) solo.getView(R.id.riderStartLocation);
        TextView end = (TextView) solo.getView(R.id.riderEndLocation);
        String start1 = start.getText().toString();

        //Adding a random destination to the screen so we can test that its value doesn't change
        solo.clickOnScreen(200, 400);
        solo.clickOnButton("Destination");
        solo.sleep(2000);

        String end1 = end.getText().toString();

        solo.clickOnView(start);
        solo.clickOnButton("OK");

        assertEquals(start1, start.getText().toString());
        assertEquals(end1, end.getText().toString());
    }


    /**
     * Testing the ride request button
     */
    @Test
    public void testBlankRideRequest() {
        solo.assertCurrentActivity("Wrong activity", RiderMapActivity.class);
        RiderMapActivity activity = (RiderMapActivity) solo.getCurrentActivity();

        solo.sleep(10000);

        solo.clickOnButton("Request Ride");
        solo.assertCurrentActivity("wrong activity", RiderMapActivity.class);

        //Adding a random destination to the screen so we can test the ride request button
        solo.clickOnScreen(200, 400);
        solo.clickOnButton("Destination");
        solo.sleep(2000);

        solo.clickOnButton("Request Ride");
        solo.sleep(2000);

        assertTrue(solo.searchText("Recommended Cost"));
    }

    /**
     * Testing the offer textview is set automatically to the recommended cost
     */
    @Test
    public void testCostOffer() {
        solo.assertCurrentActivity("wrong activity", RiderMapActivity.class);

        solo.sleep(10000);

        //Adding a random destination to the screen so we can get to the confirmation fragment
        solo.clickOnScreen(200, 400);
        /*solo.waitForView(R.id.pickDestPopUp);*/
        solo.clickOnButton("Destination");
        solo.sleep(2000);

        solo.clickOnButton("Request Ride");
        solo.sleep(2000);

        assertTrue(solo.searchText("Recommended Cost"));
        TextView offer = (TextView) solo.getView(R.id.recCostValue);
        String offerValue = offer.getText().toString();
        EditText yourOffer = (EditText) solo.getView(R.id.yourOfferValue);
        String yourOfferValue = yourOffer.getText().toString();

        assertEquals(offerValue, yourOfferValue);
    }

    /**
     * Testing the offer needs to have some value in it
     */
    @Test
    public void testBlankCostOffer() {
        solo.assertCurrentActivity("wrong activity", RiderMapActivity.class);

        solo.sleep(10000);

        //Adding a random destination to the screen so we can get to the confirmation fragment
        solo.clickOnScreen(200, 400);
        /*solo.waitForView(R.id.pickDestPopUp);*/
        solo.clickOnButton("Destination");
        solo.sleep(2000);

        solo.clickOnButton("Request Ride");
        solo.sleep(2000);

        assertTrue(solo.searchText("Recommended Cost"));
        EditText yourOffer = (EditText) solo.getView(R.id.yourOfferValue);
        yourOffer.setText("");

        solo.clickOnButton("OK");
        assertTrue(solo.searchText("Fare offer cannot be empty"));
    }

    /**
     * Testing the offer needs to be at least the recommend cost
     */
    @Test
    public void testLowCostOffer() {
        solo.assertCurrentActivity("wrong activity", RiderMapActivity.class);

        solo.sleep(10000);

        //Adding a random destination to the screen so we can get to the confirmation fragment
        solo.clickOnScreen(200, 400);
        /*solo.waitForView(R.id.pickDestPopUp);*/
        solo.clickOnButton("Destination");
        solo.sleep(2000);

        solo.clickOnButton("Request Ride");
        solo.sleep(2000);

        assertTrue(solo.searchText("Recommended Cost"));
        EditText yourOffer = (EditText) solo.getView(R.id.yourOfferValue);
        yourOffer.setText("2.00");

        solo.clickOnButton("OK");
        assertTrue(solo.searchText("Offer must be at least the recommended"));
    }


    /**
     * Closes activity after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}
