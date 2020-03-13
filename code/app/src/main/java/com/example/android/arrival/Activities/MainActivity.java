package com.example.android.arrival.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.arrival.Dialogs.DisplayQRDialog;
import com.example.android.arrival.Dialogs.ScanQRDialog;
import com.example.android.arrival.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RequestCallbackListener, ScanQRDialog.OnFragmentInteractionListener {

    private static final String TAG = "main-activity";
    private Button openScannerBTN;
    private Button genQRBTN;
    private Button btnLogout;
    private EditText inputText;
    private static final int CAMERA_REQUEST = 100;

    private Button btnRequestTest;


    @RequiresApi(api = Build.VERSION_CODES.M)
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

        checkPermissions(getApplicationContext());

        // bind views
        openScannerBTN = findViewById(R.id.scanner);
        genQRBTN = findViewById(R.id.genQR);
        inputText = findViewById(R.id.text_to_convert);
        btnRequestTest = findViewById(R.id.btnRequestTest);
        btnLogout = findViewById(R.id.btnSignOut);


        genQRBTN.setOnClickListener(view -> {
            if (inputText.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Input Text to convert", Toast.LENGTH_SHORT).show();
            }
            else {
                Log.d("generateClicked", "onClick: failed");
                FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
                DisplayQRDialog displayQRDialog = DisplayQRDialog.newInstance(inputText.getText().toString());
                displayQRDialog.show(fm, "generate");
            }
        });

        openScannerBTN.setOnClickListener(view -> openScanner());

        btnRequestTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RequestTestActivity.class);
                startActivity(intent);
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void openScanner() {
        ScanQRDialog scanQRDialog = new ScanQRDialog();
        scanQRDialog.show(getSupportFragmentManager(), "scan");
    }

//    @Override
//    public void onDonePressed(String s) {
//        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
//    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermissions(Context context){

        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }

    }
    /**
     * Temporary method that allows the testing of the RequestManager class.
     * Will be moved to the unit tests later.
     */
    public void testRequestManager() {
        // Get RequestManager singleton instance.
        RequestManager rm = RequestManager.getInstance();

        // Create new request and open it using the RequestManager.
        Request req = new Request("hello", new GeoLocation(), new GeoLocation(), 6.9f);
        rm.openRequest(req);

        // Update request.
        req.setFare(4.20f);
        rm.updateRequest(req);

        // Retrieve request history of User "user".
        rm.getRiderRequests("user", this);

        // Delete request, only used for testing.
        rm.deleteRequest(req.getID());

        // Retrieve a list of all requests currently open.
        rm.getOpenRequests(this);
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
