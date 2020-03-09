package com.example.android.arrival.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.android.arrival.R;

public class RegistrationActivity extends AppCompatActivity {

    EditText Name;
    EditText phoneNumber;
    EditText email;
    EditText password;
    EditText confirmPassword;
    Button driverSignUp;
    Button riderSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);


    }
}
