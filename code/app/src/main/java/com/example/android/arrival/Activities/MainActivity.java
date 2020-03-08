package com.example.android.arrival.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.android.arrival.Model.GeoLocation;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.RequestCallbackListener;
import com.example.android.arrival.Model.RequestManager;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class MainActivity extends AppCompatActivity implements RequestCallbackListener {

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

        // FireStore test. This should be moved to the unit testing later.
        RequestManager rm = RequestManager.getInstance();

        // Create new request and open it using the RequestManager
        // Request req = new Request(new Rider("user", "pass"), new GeoLocation(), new GeoLocation(), 6.9f);
        Request req = new Request(new Rider("user", "pass").getUsername(), new GeoLocation(), new GeoLocation(), 6.9f);
        Request req1 = new Request(new Rider("user2", "pass").getUsername(), new GeoLocation(), new GeoLocation(), 6.9f);
        Request req2 = new Request(new Rider("user3", "pass").getUsername(), new GeoLocation(), new GeoLocation(), 6.9f);
        rm.openRequest(req);
        rm.openRequest(req1);
        rm.openRequest(req2);
        req.setFare(4.20f);
        rm.updateRequest(req);
        rm.getRiderRequests("user", this);
        rm.deleteRequest(req.getID());
        rm.deleteRequest(req1.getID());
        rm.deleteRequest(req2.getID());

        rm.getOpenRequests(this);
    }

    public void openMaps(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCallbackStart() {

    }

    @Override
    public void onGetOpenSuccess(QuerySnapshot snapshot){
        // Convert the snapshot to objects that can be used to display information
        List<Request> openRequests = snapshot.toObjects(Request.class);
        Log.d("MainActivity" + "-getOpen", openRequests.toString());
    }

    @Override
    public void onGetRiderRequestsSuccess(QuerySnapshot snapshot) {
        // Convert the snapshot to objects that can be used to display information
        List<Request> userRequests = snapshot.toObjects(Request.class);
        Log.d("MainActivity" + "-getReq", userRequests.toString());
    }

    @Override
    public void onGetDriverRequestsSuccess(QuerySnapshot snapshot) {
        // Convert the snapshot to objects that can be used to display information
        List<Request> userRequests = snapshot.toObjects(Request.class);
        Log.d("MainActivity" + "-getReq", userRequests.toString());
    }
}
