package com.example.android.arrival.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.arrival.Dialogs.ScanQRDialog;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.Model.User;
import com.example.android.arrival.Util.AccountCallbackListener;
import com.example.android.arrival.Util.AccountManager;
import com.example.android.arrival.Util.RequestCallbackListener;
import com.example.android.arrival.Util.RequestManager;
import com.example.android.arrival.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Drivers map, contains the driver's locations, markers of open requests
//when marker is pressed info pops up about marker

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback, RequestCallbackListener, ScanQRDialog.OnFragmentInteractionListener, AccountCallbackListener {

    private static final String TAG = "DriverMapActivity";
    private static final int CAMERA_REQUEST = 100;
    private static final int REFRESH_INTERVAL = 1000 * 45; // 45 seconds in millis

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private static final int REQUEST_USER_LOCATION_CODE = 99;
    //Youtube video by SimCoder https://www.youtube.com/watch?v=u10ZEnARZag&t=857s
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String driverName;
    public boolean zoom = true;
    static boolean currentActivity = false;
    private int index;

    ArrayList<Request> requestsList = new ArrayList<>();

    private FirebaseFirestore fb;
    private RequestManager rm;

    private Handler handler;

    private Request currRequest;

    private EditText txtDriverLocation;
    private EditText txtRiderLocation;
    private Button btnCancelRide;
    private Button btnConfirmPickup;
    private Button btnCompleteRide;
    private Button btnScanQR;
    private Button btnSignOut;
    private FloatingActionButton btnRefresh;
    private TextView txtStatus;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_map_activity);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fb = FirebaseFirestore.getInstance();
        rm = RequestManager.getInstance();
        AccountManager.getInstance().getUserData(DriverMapActivity.this);

        handler = new Handler();

        // Get camera permissions
        checkPermissions(getApplicationContext());

        // Bind components
        txtDriverLocation = findViewById(R.id.txtDriverLocation);
        txtRiderLocation = findViewById(R.id.txtRiderLocation);
        btnCancelRide = findViewById(R.id.driverCancelRide);
        btnConfirmPickup = findViewById(R.id.driverConfirmPickup);
        btnCompleteRide = findViewById(R.id.driverCompleteRide);
        btnScanQR = findViewById(R.id.btnDriverScanQR);
        btnSignOut = findViewById(R.id.btnDriverSignout);
        btnRefresh = findViewById(R.id.btnDriverRefresh);
        txtStatus = findViewById(R.id.txtDriverStatus);

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btnSignOut Clicked");
                Log.d(TAG, "Attempting to sign out user... ");
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnCancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert (currRequest != null);

                Log.d(TAG, "btnCancelRide clicked");
                currRequest.setDriver(null);
                currRequest.setStatus(Request.OPEN);
                rm.updateRequest(currRequest, (RequestCallbackListener) v.getContext());
                btnCancelRide.setVisibility(View.INVISIBLE);
                btnConfirmPickup.setVisibility(View.INVISIBLE);
                rm.getOpenRequests(DriverMapActivity.this);
            }
        });

        btnConfirmPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert currRequest != null;

                Log.d(TAG, "btnConfirmPickup clicked");
                currRequest.setStatus(Request.PICKED_UP);
                rm.updateRequest(currRequest, (RequestCallbackListener) v.getContext());
            }
        });

        btnCompleteRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currRequest.setStatus(Request.AWAITING_PAYMENT);
                rm.updateRequest(currRequest, (RequestCallbackListener) v.getContext());
                refresh();
            }
        });

        btnScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Opening scanner...");
                openScanner();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        updateInfo();
    }

    public void refresh() {
        Log.d(TAG, "refreshing...");
        if(currRequest!=null) {
            rm.getRequest(currRequest.getID(), this);
        } else {
            // FOR TESTING: If you want to test and spare the time of
            // creating a new request, uncomment this line. Then you
            // can just manipulate it in FireBase and refresh with the
            // refresh button. Ex. changing status. Doc w/ ID = 1
            // rm.getRequest("1", this);
            rm.getOpenRequests(this);
        }
    }

    private Runnable periodicUpdate = new Runnable() {
        @Override
        public void run() {
            if(handler!= null) {
                handler.postDelayed(periodicUpdate, REFRESH_INTERVAL);
                refresh();
            } else {
                handler = new Handler();
            }
        }
    };

    public void updateInfo() {
        if (currRequest == null) {
            rm.getOpenRequests(this);

            btnCancelRide.setVisibility(View.INVISIBLE);
            btnConfirmPickup.setVisibility(View.INVISIBLE);
            btnCompleteRide.setVisibility(View.INVISIBLE);
            btnScanQR.setVisibility(View.INVISIBLE);
            txtRiderLocation.setText("");
            txtStatus.setText("");
        } else {
            Log.d(TAG, "currRequest is " + currRequest.toString());
            txtStatus.setText(Request.STATUS.get(currRequest.getStatus()));

            requestsList.clear();
            txtRiderLocation.setText(currRequest.getStartLocation().getAddress());

            if (currRequest.getStatus() == Request.ACCEPTED) {
                // Clear open requests from map, except currRequest
                mMap.clear();
                MarkerOptions mop = new MarkerOptions();
                mop.position(currRequest.getStartLocation().getLatLng());
                mMap.addMarker(mop);

                Log.d("notifications", currRequest.getRider());

//                //Testing notification
//                notif.sendNotification();
//                String TOKEN = fb.collection("riders").document(currRequest.getRider()).get().;

                btnCancelRide.setVisibility(View.VISIBLE);
                btnConfirmPickup.setVisibility(View.VISIBLE);
                btnCompleteRide.setVisibility(View.INVISIBLE);
                btnScanQR.setVisibility(View.INVISIBLE);
            } else if (currRequest.getStatus() == Request.PICKED_UP) {
                // Clear open requests from map, except currRequest destination
                mMap.clear();
                MarkerOptions mop = new MarkerOptions();
                mop.position(currRequest.getEndLocation().getLatLng());
                mMap.addMarker(mop);

                btnCancelRide.setVisibility(View.INVISIBLE);
                btnConfirmPickup.setVisibility(View.INVISIBLE);
                btnCompleteRide.setVisibility(View.VISIBLE);
                btnScanQR.setVisibility(View.INVISIBLE);

            } else if(currRequest.getStatus() == Request.AWAITING_PAYMENT) {
                // Clear open requests from map, except currRequest destination
                mMap.clear();
                MarkerOptions mop = new MarkerOptions();
                mop.position(currRequest.getEndLocation().getLatLng());
                mMap.addMarker(mop);

                btnCancelRide.setVisibility(View.INVISIBLE);
                btnConfirmPickup.setVisibility(View.INVISIBLE);
                btnCompleteRide.setVisibility(View.INVISIBLE);
                btnScanQR.setVisibility(View.VISIBLE);

            } else if (currRequest.getStatus() == Request.COMPLETED) {
                rm.getOpenRequests(this);

                btnCancelRide.setVisibility(View.INVISIBLE);
                btnConfirmPickup.setVisibility(View.INVISIBLE);
                btnCompleteRide.setVisibility(View.INVISIBLE);
                btnScanQR.setVisibility(View.INVISIBLE);
                txtRiderLocation.setText("");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermissions(Context context){
        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }
    }

    public void openScanner() {
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        ScanQRDialog scanQRDialog = new ScanQRDialog();
        scanQRDialog.show(fm, "scan");
    }

    /**
     * Create map with current location and switch to fragment on marker press
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                mMap.setMyLocationEnabled(true);
            } else {
                checkUserLocationPermission();
            }
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Share marker and requests with the fragment
                for (int i = 0; i < requestsList.size(); i++) {
                    if (requestsList.get(i).getStartLocation().getLat() == marker.getPosition().latitude && requestsList.get(i).getStartLocation().getLon() == marker.getPosition().longitude) {
                        index = i;
                        currRequest = requestsList.get(i);
                    }
                }

                ArrayList<Marker> markers = new ArrayList<>();
                markers.add(marker);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Bundle args = new Bundle();
                args.putSerializable("currentRequest", currRequest);
                args.putSerializable("markerLocation", markers);
                args.putSerializable("driverName", driverName);
                args.putSerializable("driverLat", currentLocation.getLatitude());
                args.putSerializable("driverLon", currentLocation.getLongitude());

                AcceptRequestConfFrag acceptRequestConfFrag = new AcceptRequestConfFrag();
                acceptRequestConfFrag.setArguments(args);
                fragmentTransaction.add(0, acceptRequestConfFrag);
                fragmentTransaction.commit();
                return false;
            }
        });
    }

    /**
     * Place current location and move camera towards it
     * also shares driver's location with firestore to create an available driver
     */
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                mMap.setMyLocationEnabled(true);
                currentLocation = location;

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (zoom) {
                    zoom = false;
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                }

                if (currentActivity) {

//                   Youtube video by SimCoder https://firebase.google.com/docs/firestore/manage-data/add-data
                    Map<String, Object> map = new HashMap<>();
                    map.put("lat", location.getLatitude());
                    map.put("lon", location.getLongitude());

                    fb.collection("availableDrivers").document(driverName)
                            .set(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("driverLocation", "updated");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("driverLocation", "not updated", e);
                                }
                            });
                }
            }
        }
    };


    /**
     * When map is closed stop showing the driver's location making the driver unavailable
     */
    @Override
    protected void onStop() {
        super.onStop();

        Map<String, Object> map = new HashMap<>();
        map.put("lat", null);
        map.put("lon", null);

        fb.collection("availableDrivers").document(driverName)
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("driverLocation", "null");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("driverLocation", "not null", e);
                    }
                });
    }

    /**
     * Check if the map activity is open
     */
//    Stackoverflow post by virsir https://stackoverflow.com/users/238061/virsir
//    Answer https://stackoverflow.com/questions/3262157/how-to-check-if-my-activity-is-the-current-activity-running-in-the-screen
    @Override
    public void onResume() {
        super.onResume();
        currentActivity = true;

        refresh();
        periodicUpdate.run();
    }

    /**
     * Check if the map activity is closed
     */
    @Override
    public void onPause() {
        super.onPause();
        boolean active = false;
        handler.removeCallbacks(periodicUpdate);
        currentActivity = false;
    }

    /**
     * Ask the user for location permission
     * if they do not accept then ask again when the activity is opened
     */
    public void checkUserLocationPermission() {
        //If permission is not granted the app will ask for user permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Current Location Permission Denied")
                        .setMessage("Please give access to current location.")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
            }
        }
    }


    /**
     * If the user accepts locations then show the current location
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_USER_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                    mMap.setMyLocationEnabled(true);
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onCallbackStart() {

    }

    @Override
    public void update() {

    }

    @Override
    public void onGetRequestSuccess(DocumentSnapshot snapshot) {
        Request req = snapshot.toObject(Request.class);

        txtStatus.setText(Request.STATUS.get(req.getStatus()));

        Log.d(TAG, "Retrieved request: " + req.toString());
        //Log.d(TAG, "Retrieved request: " + req.getDriver());
        Query query = fb.collection("drivers").whereEqualTo("name", req.getDriver());
        Log.d(TAG, "Query:" + query.toString());

        fb.collection("drivers").whereEqualTo("name","driver")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e!= null) {
                            Log.d(TAG, "Listen failed" + e);
                        }
                        for (DocumentSnapshot doc: queryDocumentSnapshots) {
                            Log.d(TAG, "In document snapshot");
                            Log.d(TAG, "data: " + doc.getId() + "=>" + doc.getData());
                        }
                    }
                });


        if(req.getStatus() == Request.CANCELLED) {
            currRequest = null;
        } else if(currRequest == null || !currRequest.equals(req)) {
            currRequest = req;
        }

        updateInfo();
    }

    /**
     * Add all requests to an ArrayList
     *
     * @param snapshot
     */
    @Override
    public void onGetOpenSuccess(QuerySnapshot snapshot) {
        requestsList.clear();
        requestsList.addAll(snapshot.toObjects(Request.class));

        mMap.clear();

        for (int i = 0; i < requestsList.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();

            Request request = requestsList.get(i);

            markerOptions.position(request.getStartLocation().getLatLng());
            mMap.addMarker(markerOptions);
        }
    }

    @Override
    public void onGetRiderRequestsSuccess(QuerySnapshot snapshot) {

    }

    @Override
    public void onGetDriverRequestsSuccess(QuerySnapshot snapshot) {

    }

    @Override
    public void onDonePressed(String s) {
        // TODO: I have no idea what this is supposed to do - Reilly
    }

    @Override
    public void onAccountSignIn(String userType) {

    }

    @Override
    public void onSignInFailure(String e) {

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
        User user = driver;
        driverName = user.getName();


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
}







