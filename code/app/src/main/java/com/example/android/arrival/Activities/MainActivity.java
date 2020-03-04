package com.example.android.arrival.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.android.arrival.Model.GeoLocation;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.RequestManager;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "main-activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button map_Button;

        map_Button = findViewById(R.id.map_button);

        map_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMaps();
            }
        });

        // FireStore test
        RequestManager rm = RequestManager.getInstance();

        // Create new request and open it using the RequestManager
//        Request req = new Request(new Rider("user", "pass"), new GeoLocation(), new GeoLocation(), 6.9f);
        Request req = new Request(new Rider("user", "pass").getUsername(), new GeoLocation(), new GeoLocation(), 6.9f);
        Request req1 = new Request(new Rider("user2", "pass").getUsername(), new GeoLocation(), new GeoLocation(), 6.9f);
        Request req2 = new Request(new Rider("user3", "pass").getUsername(), new GeoLocation(), new GeoLocation(), 6.9f);
        rm.openNewRequest(req);
        rm.openNewRequest(req1);
        rm.openNewRequest(req2);
        req.setFare(4.20f);
        rm.updateRequest(req);
        rm.getRiderRequests("user");
    }

    public void openMaps(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
