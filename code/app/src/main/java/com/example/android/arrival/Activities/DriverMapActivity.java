package com.example.android.arrival.Activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.arrival.Model.Request;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Drivers map, contains the driver's locations, markers of open requests
//when marker is pressed info pops up about marker

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, RequestCallbackListener {

    private static final String TAG = "DriverMapActivity";

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private static final int REQUEST_USER_LOCATION_CODE = 99;
    //Youtube video by SimCoder https://www.youtube.com/watch?v=u10ZEnARZag&t=857s
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String riderID;
    public boolean zoom = true;
    static boolean active = false;
    private int index;

    ArrayList<Request> requestsList = new ArrayList<>();

    private FirebaseFirestore fb;
    private RequestManager rm;

    private Request currRequest;

    private EditText txtDriverLocation;
    private EditText txtRiderLocation;
    private Button btnCancelRide;
    private Button btnConfirmPickup;
    private Button btnCompleteRide;
    private Button btnSignOut;
    private FloatingActionButton btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_map_activity);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fb = FirebaseFirestore.getInstance();
        rm = RequestManager.getInstance();

        // Bind components
        txtDriverLocation = findViewById(R.id.txtDriverLocation);
        txtRiderLocation = findViewById(R.id.txtRiderLocation);
        btnCancelRide = findViewById(R.id.driverCancelRide);
        btnConfirmPickup = findViewById(R.id.driverConfirmPickup);
        btnCompleteRide = findViewById(R.id.driverCompleteRide);
        btnSignOut = findViewById(R.id.btnDriverSignout);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btnSignOut Clicked");
                Log.d(TAG, "Attempting to sign out user... ");
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnCancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert (currRequest != null);

                Log.d(TAG, "btnCancelRide clicked");
                currRequest.setDriver(null);
                currRequest.setStatus(Request.STATUS_OPEN);
                rm.updateRequest(currRequest, (RequestCallbackListener) v.getContext());
            }
        });

        btnConfirmPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert currRequest != null;

                Log.d(TAG, "btnConfirmPickup clicked");
                currRequest.setStatus(Request.STATUS_PICKED_UP);
                rm.updateRequest(currRequest, (RequestCallbackListener) v.getContext());
            }
        });

        btnCompleteRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currRequest.setStatus(Request.STATUS_COMPLETED);
                rm.updateRequest(currRequest, (RequestCallbackListener) v.getContext());
                // TODO: Bring up QR scanner
                refresh();
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

        refresh();
    }

    public void refresh() {
        Log.d(TAG, "refreshing...");

        if (currRequest == null) {
            rm.getOpenRequests(this);

            btnCancelRide.setVisibility(View.INVISIBLE);
            btnConfirmPickup.setVisibility(View.INVISIBLE);
            btnCompleteRide.setVisibility(View.INVISIBLE);
            txtRiderLocation.setText("");
        } else {
            Log.d(TAG, "currRequest is " + currRequest.toString());

            requestsList.clear();
            txtRiderLocation.setText(currRequest.getStartLocation().getAddress());

            if (currRequest.getStatus() == Request.STATUS_ACCEPTED) {
                // Clear open requests from map, except currRequest
                mMap.clear();
                MarkerOptions mop = new MarkerOptions();
                mop.position(currRequest.getStartLocation().getLatLng());
                mMap.addMarker(mop);

                btnCancelRide.setVisibility(View.VISIBLE);
                btnConfirmPickup.setVisibility(View.VISIBLE);
                btnCompleteRide.setVisibility(View.INVISIBLE);
            } else if (currRequest.getStatus() == Request.STATUS_PICKED_UP) {
                // Clear open requests from map, except currRequest destination
                mMap.clear();
                MarkerOptions mop = new MarkerOptions();
                mop.position(currRequest.getEndLocation().getLatLng());
                mMap.addMarker(mop);

                btnCancelRide.setVisibility(View.INVISIBLE);
                btnConfirmPickup.setVisibility(View.INVISIBLE);
                btnCompleteRide.setVisibility(View.VISIBLE);
            } else if(currRequest.getStatus() == Request.STATUS_COMPLETED) {
                rm.getOpenRequests(this);

                btnCancelRide.setVisibility(View.INVISIBLE);
                btnConfirmPickup.setVisibility(View.INVISIBLE);
                btnCompleteRide.setVisibility(View.INVISIBLE);
                txtRiderLocation.setText("");
            }
        }
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

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (zoom) {
                    zoom = false;
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                }

                if (active) {

//                   Youtube video by SimCoder https://firebase.google.com/docs/firestore/manage-data/add-data
                    Map<String, Object> map = new HashMap<>();
                    map.put("lat", location.getLatitude());
                    map.put("lon", location.getLongitude());

                    fb.collection("availableDrivers").document("driver1")
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

        fb.collection("availableDrivers").document("driver1")
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
        active = true;

        refresh();
    }

    /**
     * Check if the map activity is closed
     */
    @Override
    public void onPause() {
        super.onPause();
        active = false;
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
        Log.d(TAG, "Retrieved req: " + req.toString());
        if (req != null) {
            if (currRequest == null) {
                currRequest = req;
            } else if (req.getStatus() == Request.STATUS_OPEN) {
                currRequest = null;
            }
        } else {
            currRequest = null;
        }

        refresh();
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
}







