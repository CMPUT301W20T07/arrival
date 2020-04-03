package com.example.android.arrival.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.arrival.Model.Request;
import com.example.android.arrival.R;
import com.example.android.arrival.Util.RequestAdapter;

import java.util.ArrayList;

public class RideHistoryActivity extends AppCompatActivity {

    private static final String TAG = "RideHistoryActivity";
    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Request> requests;

    private String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);
        setTheme(R.style.AppThemeActionBar);
        Intent intent = getIntent();

        requests = (ArrayList<Request>) intent.getSerializableExtra("list");
        type = intent.getStringExtra("accountType");
        Log.d(TAG, "onCreate: " + requests);
        adapter = new RequestAdapter(this, requests, type);
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }



}
