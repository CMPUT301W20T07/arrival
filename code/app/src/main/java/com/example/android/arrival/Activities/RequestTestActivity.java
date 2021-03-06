package com.example.android.arrival.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.arrival.Model.Place;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.R;
import com.example.android.arrival.Util.RequestCallbackListener;
import com.example.android.arrival.Util.RequestManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Android Activity used only for unit testing the RequestManager.
 */
public class RequestTestActivity extends AppCompatActivity implements RequestCallbackListener {

    private static final String TAG = "ReqTest";

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
                Request req = new Request(newName.getText().toString(), new Place(), new Place(), 13.37f );
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

    }

    @Override
    public void onGetRequestSuccess(DocumentSnapshot snapshot) {
        Request req = snapshot.toObject(Request.class);
        Log.d(TAG + "-getReq", req.toString());
        rm.getOpenRequests(this);
    }

    @Override
    public void onGetOpenSuccess(QuerySnapshot snapshot){
        // Convert the snapshot to objects that can be used to display information
        List<Request> openRequests = snapshot.toObjects(Request.class);
        requestAdapter.clear();
        requestAdapter.addAll(openRequests);
        requestAdapter.notifyDataSetChanged();
        Log.d(TAG + "-getOpen", openRequests.toString());
    }

    @Override
    public void onGetRiderRequestsSuccess(QuerySnapshot snapshot) {
        // Convert the snapshot to objects that can be used to display information
        List<Request> userRequests = snapshot.toObjects(Request.class);
        Log.d(TAG + "-getReqs", userRequests.toString());
    }

    @Override
    public void onGetRiderOpenRequestsSuccess(QuerySnapshot snapshot) {

    }

    @Override
    public void onGetDriverRequestsSuccess(QuerySnapshot snapshot) {
        // Convert the snapshot to objects that can be used to display information
        List<Request> userRequests = snapshot.toObjects(Request.class);
        Log.d(TAG + "-getReqs", userRequests.toString());
    }
}
