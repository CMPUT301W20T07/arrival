package com.example.android.arrival;

import com.example.android.arrival.Activities.LoginActivity;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class LoginUnitTest {

    LoginActivity email;
    LoginActivity password;


    @Test
    void testEmptyEmailField() {

        LoginActivity emptyEmail = new LoginActivity();
        assertTrue(email.toString().equals(""));
    }

    @Test
    void testValidEmail() {
        LoginActivity validEmail = new LoginActivity();
        assertTrue(email.toString().contains("@"));
    }

    @Test
    void testPasswordEmailField() {

        LoginActivity EmptyPassword = new LoginActivity();
        assertTrue(password.toString().equals(""));
    }
}
