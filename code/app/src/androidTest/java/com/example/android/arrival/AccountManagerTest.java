package com.example.android.arrival;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.android.arrival.Activities.LoginActivity;
import com.example.android.arrival.Activities.RegistrationActivity;
import com.example.android.arrival.Activities.RiderMapActivity;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AccountManagerTest {

    private Solo solo;
    private static final String WRONG_ACT = "wrong activity";
    private static final String TAG = "AMTest";

    @Rule
    public ActivityTestRule<LoginActivity> rule = new ActivityTestRule<>(LoginActivity.class, true, true);

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



    @Test
    public void testValidRiderSignIn() {

        String emailStr = "accman1@test.ca";
        String wrongPassword = "heello";
        String correctPassword = "hello1";

        solo.assertCurrentActivity(WRONG_ACT, LoginActivity.class);



        EditText email = (EditText) solo.getEditText("Email");
        EditText password = (EditText) solo.getEditText("Password");
        //Button signIn = (Button) solo.getButton(R.id.sign_in_button);

        solo.enterText(email, emailStr);
        solo.enterText(password, wrongPassword);
        solo.clickOnButton(0);

        solo.sleep(1000);
        solo.clearEditText(password);
        solo.enterText(password, correctPassword);

        solo.clickOnButton(0);

        solo.waitForActivity(RiderMapActivity.class);
        solo.assertCurrentActivity(WRONG_ACT, RiderMapActivity.class);


    }

    @Test
    public void testRiderRegistration() {

        solo.assertCurrentActivity(WRONG_ACT, LoginActivity.class);

        solo.clickOnText("Sign Up");

        solo.waitForActivity(RegistrationActivity.class);
        solo.clickOnButton("Rider");
        solo.assertCurrentActivity(WRONG_ACT, RegistrationActivity.class);
        solo.clickOnButton("Driver");
        solo.clickOnButton("Done");
        solo.assertCurrentActivity(WRONG_ACT, RegistrationActivity.class);
        solo.clickOnImage(1);
        solo.waitForDialogToOpen();
        solo.clickOnText("Select photo");
        solo.clickOnImage(0);
        solo.assertCurrentActivity(WRONG_ACT, RegistrationActivity.class);

        String name = "solo testUser";
        String email = "solotest@hotmail.com";
        String wrongEmail = "solotest";


    }


}
