package com.example.android.arrival.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.android.arrival.Model.GeoLocation;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.RequestCallbackListener;
import com.example.android.arrival.Model.RequestManager;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Temporary class for testing the functionality of the RequestManager.
 *
 * Activity layout code based on the Lab 7 starter code: https://github.com/Jakaria08/ListCity
 */
public class RequestTestActivity extends AppCompatActivity implements RequestCallbackListener {

    private RequestManager rm;

    private Button btnOpenReq;
    private Button btnRefresh;
    private Button btnConfirm;
    private ListView reqList;

    private LinearLayout nameField;
    private EditText newName;

    private ArrayAdapter<Request> requestAdapter;
    private ArrayList<Request> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_test);

        rm = RequestManager.getInstance();

        btnOpenReq = findViewById(R.id.btn_open);
        btnRefresh = findViewById(R.id.button_refresh);
        btnConfirm = findViewById(R.id.button_confirm);
        reqList = findViewById(R.id.req_list);

        nameField = findViewById(R.id.field_nameEntry);
        newName = findViewById(R.id.editText_name);

        dataList = new ArrayList<>();
        requestAdapter = new ArrayAdapter<>(this, R.layout.content, dataList);
        reqList.setAdapter(requestAdapter);

        btnOpenReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameField.setVisibility(View.VISIBLE);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open req
                Request req = new Request(newName.getText().toString(), new GeoLocation(), new GeoLocation(), 13.37f );
                rm.openRequest(req, (RequestCallbackListener) v.getContext());

                newName.getText().clear();
                nameField.setVisibility(View.INVISIBLE);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rm.getOpenRequests((RequestCallbackListener) v.getContext());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        rm.getOpenRequests(this);
    }

    @Override
    public void onCallbackStart() {
        // Display loading...
    }

    @Override
    public void update() {
        rm.getOpenRequests(this);
    }

    @Override
    public void onGetOpenSuccess(QuerySnapshot snapshot){
        // Convert the snapshot to objects that can be used to display information
        List<Request> openRequests = snapshot.toObjects(Request.class);
        requestAdapter.clear();
        requestAdapter.addAll(openRequests);
        requestAdapter.notifyDataSetChanged();
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
