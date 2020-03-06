package com.example.android.arrival.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.android.arrival.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button riderMapButton = (Button) findViewById(R.id.riderMapButton);

        riderMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRiderMaps();
            }
        });

        Button driverMapButton;
        driverMapButton = findViewById(R.id.driverMapButton);

        driverMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDriverMaps();
            }
        });
    }

    public void openRiderMaps(){
        Intent intent = new Intent(this, RiderMapActivity.class);
        startActivity(intent);
    }

    public void openDriverMaps(){
        Intent intent = new Intent(this, DriverMapActivity.class);
        startActivity(intent);
    }
}
