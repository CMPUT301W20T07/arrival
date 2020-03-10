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

public class RequestTestActivity extends AppCompatActivity implements RequestCallbackListener {

    private RequestManager rm;

    private Button btnOpenReq;
    private Button btnClear;
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
        btnClear = findViewById(R.id.button_clear);
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
                Log.d("RequestTest", "clicked!");
                Request req = new Request(newName.getText().toString(), new GeoLocation(), new GeoLocation(), 13.37f );
                openReq(req);
                newName.getText().clear();
                nameField.setVisibility(View.INVISIBLE);
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAdapter.clear();
            }
        });

    }

    /**
     * Temporary method that allows the testing of the RequestManager class.
     * Will be moved to the unit tests later.
     */
    public void testRequestManager() {
        // Create new request and open it using the RequestManager.
        Request req = new Request(new Rider("user", "pass").getUsername(), new GeoLocation(), new GeoLocation(), 6.9f);
        rm.openRequest(req, this);

        // Update request.
        req.setFare(4.20f);
        rm.updateRequest(req, this);

        // Retrieve request history of User "user".
        rm.getRiderRequests("user", this);

        // Delete request, only used for testing.
        rm.deleteRequest(req.getID(), this);

        // Retrieve a list of all requests currently open.
        rm.getOpenRequests(this);
    }

    public void openReq(Request req) {
        rm.openRequest(req, this);
    }

    @Override
    public void onCallbackStart() {

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
