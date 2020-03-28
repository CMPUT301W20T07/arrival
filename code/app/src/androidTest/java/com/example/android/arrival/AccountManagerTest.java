package com.example.android.arrival;

import android.app.Activity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.android.arrival.Activities.LoginActivity;
import com.example.android.arrival.Activities.RiderMapActivity;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AccountManagerTest {

    private Solo solo;
    private static final String WRONG_ACT = "wrong activity";

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

        solo.assertCurrentActivity(WRONG_ACT, LoginActivity.class);
        EditText email = (EditText) solo.getEditText(R.id.login_email_editText);
        EditText password = (EditText) solo.getEditText(R.id.login_passWord_editText);
        Button signIn = (Button) solo.getButton(R.id.sign_in_button);
        email.setText("accman@test.ca");
        password.setText("hello1");
        signIn.performClick();

        //solo.waitForText()
    }


}
