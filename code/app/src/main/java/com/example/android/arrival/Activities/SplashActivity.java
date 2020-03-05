package com.example.android.arrival.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.arrival.R;

public class SplashActivity extends AppCompatActivity {

    //Setting the timer for the Splash Screen
    private static int SPLASH_TIME_OUT = 1000;
    //Times out after 2 seconds and moves to the login screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Sets the splash screen to cover the entire screen of the app window


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

                startActivity(intent);

                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
