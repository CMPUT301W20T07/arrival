package com.example.android.arrival.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.RequestCallbackListener;
import com.example.android.arrival.Model.RequestManager;
import com.example.android.arrival.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Drivers map, contains the driver's locations, markers of open requests
//when marker is pressed info pops up about marker

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, RequestCallbackListener {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private static final int REQUEST_USER_LOCATION_CODE = 99;
    //Youtube video by SimCoder https://www.youtube.com/watch?v=u10ZEnARZag&list=PLxabZQCAe5fgXx8cn2iKOtt0VFJrf5bOd&index=42
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String riderID;
    public boolean zoom = true;
    static boolean active = false;
    ArrayList<Request> requestsList = new ArrayList<>();
    ArrayList<Marker> markers = new ArrayList<>();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loadOpenRequests();

    }

    /**
     * Create map with current location and switch to fragment on marker press
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                mMap.setMyLocationEnabled(true);
            }
            else {
                checkUserLocationPermission();
            }
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Share marker and requests with the fragment
                markers.add(marker);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Bundle args = new Bundle();
                args.putSerializable("requestsList", requestsList);
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
            for (Location location : locationResult.getLocations())
            {
                mMap.setMyLocationEnabled(true);

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (zoom) {
                    zoom = false;
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                }

                if(active) {

//                   Youtube video by SimCoder https://firebase.google.com/docs/firestore/manage-data/add-data
                    Map<String, Object> map = new HashMap<>();
                    map.put("lat", location.getLatitude());
                    map.put("lon", location.getLongitude());

                    firebaseFirestore.collection("availableDrivers").document("driver1")
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

        firebaseFirestore.collection("availableDrivers").document("driver1")
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
    public void checkUserLocationPermission(){
        //If permission is not granted the app will ask for user permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(this)
                        .setTitle("Current Location Permission Denied")
                        .setMessage("Please give access to current location.")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
            }
        }
    }


    /**
     * If the user accepts locations then show the current location
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch(requestCode)
        {
            case REQUEST_USER_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                    mMap.setMyLocationEnabled(true);
                }
                else
                {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    /**
     * Get open requests
     */
    public void loadOpenRequests() {
        RequestManager.getInstance().getOpenRequests(this);

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

    /**
     * Add all requests to an ArrayList
     * @param snapshot
     */
    @Override
    public void onGetOpenSuccess(QuerySnapshot snapshot) {

        for (DocumentSnapshot snapshot2 : snapshot) {
            requestsList.add(snapshot2.toObject(Request.class));
        }


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
