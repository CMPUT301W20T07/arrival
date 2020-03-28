package com.example.android.arrival;

import android.widget.EditText;

import com.example.android.arrival.Activities.LoginActivity;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class LoginUnitTest {

    EditText email;
    EditText password;


    @Test
    void testEmptyEmailField() {

        LoginActivity emptyEmail = new LoginActivity();
        email = emptyEmail.findViewById(R.id.login_email_editText);
        String email_text = String.valueOf(email.getText());
        assertTrue(email_text.equals(""));
    }

    @Test
    void testValidEmail() {
        LoginActivity validEmail = new LoginActivity();
        email = validEmail.findViewById(R.id.login_email_editText);
        String email_text = String.valueOf(email.getText());
        assertTrue(email_text.contains("@"));
    }

    @Test
    void testPasswordEmailField() {

        LoginActivity EmptyPassword = new LoginActivity();
        password = EmptyPassword.findViewById(R.id.login_passWord_editText);
        String password_text = String.valueOf(password.getText());
        assertTrue(password_text.equals(""));
    }
}
