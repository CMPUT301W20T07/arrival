package com.example.android.arrival.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.example.android.arrival.Util.AccountCallbackListener;
import com.example.android.arrival.Util.AccountManager;
import com.example.android.arrival.Util.RequestAdapter;
import com.example.android.arrival.Util.RequestCallbackListener;
import com.example.android.arrival.Util.RequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import io.opencensus.stats.Measurement;

public class RideHistoryActivity extends AppCompatActivity implements RequestCallbackListener, AccountCallbackListener {

    private RequestManager requestManager;
    private AccountManager accountManager;
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";
    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Request> requests;
    private String uid;
    private String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        accountManager = AccountManager.getInstance();
        requestManager = RequestManager.getInstance();
        uid = accountManager.getUID();
        accountManager.getAccountType(uid, this);

    }

    @Override
    public void onAccountTypeRetrieved(String userType) {
        if (userType.equals(RIDER_TYPE_STRING)) {
            requestManager.getRiderRequests(uid, this);
            type = userType;
        }

        else if (userType.equals(DRIVER_TYPE_STRING)) {
            requestManager.getDriverRequests(uid, this);
            type = userType;
        }
    }

    @Override
    public void onAccountTypeRetrieveFailure(String e) {
        Toast.makeText(this, e, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAccountCreated(String accountType) {

    }

    @Override
    public void onAccountCreationFailure(String e) {

    }

    @Override
    public void onRiderDataRetrieved(Rider rider) {

    }

    @Override
    public void onDriverDataRetrieved(Driver driver) {

    }

    @Override
    public void onDataRetrieveFail(String e) {

    }

    @Override
    public void onAccountDeleted() {

    }

    @Override
    public void onAccountDeleteFailure(String e) {

    }

    @Override
    public void onImageUpload() {

    }

    @Override
    public void onImageUploadFailure(String e) {

    }

    @Override
    public void onPhotoReceived(Uri uri) {

    }

    @Override
    public void onPhotoReceiveFailure(String e) {

    }

    @Override
    public void onAccountUpdated() {

    }

    @Override
    public void onAccountUpdateFailure(String e) {

    }

    @Override
    public void onCallbackStart() {

    }

    @Override
    public void update() {

    }

    @Override
    public void onGetRequestSuccess(DocumentSnapshot snapshot) {

    }

    @Override
    public void onGetOpenSuccess(QuerySnapshot snapshot) {

    }

    @Override
    public void onGetRiderRequestsSuccess(QuerySnapshot snapshot) {
        // put requests into list that can be passed to the adapter
        for (QueryDocumentSnapshot document : snapshot) {
            Request request = document.toObject(Request.class);
            requests.add(request);
        }
        adapter = new RequestAdapter(this, requests, type);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onGetDriverRequestsSuccess(QuerySnapshot snapshot) {
        // put requests into list that can be passed to the adapter
        for (QueryDocumentSnapshot document : snapshot) {
            Request request = document.toObject(Request.class);
            requests.add(request);
        }
        adapter = new RequestAdapter(this, requests, type);
        recyclerView.setAdapter(adapter);
    }
}
