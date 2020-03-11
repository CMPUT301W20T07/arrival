package com.example.android.arrival.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private static final int REQUEST_USER_LOCATION_CODE = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String riderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        getCustomerInfo();

    }

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
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations())
            {
                // lastLocation might be useless
                lastLocation = location;
                mMap.setMyLocationEnabled(true);

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

//                //Get the ID of the customer
//                String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("driversAvailable");
//
//                //Share information with database creating an available driver
//                GeoFire geoFire = new GeoFire(databaseReference);
//                geoFire.setLocation(driverID, new GeoLocation(location.getLatitude(), location.getLongitude()));

            }
        }
    };

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        //Get the ID of the customer
//        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("driversAvailable");
//
//        GeoFire geoFire = new GeoFire(databaseReference);
//        //When driver closes the app remove driver from the available drivers
//        geoFire.removeLocation(driverID);
//    }

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

//    public void getCustomerInfo() {
//
//        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference assignedRiderRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
//        assignedRiderRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()) {
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//                    if(map.get("riderRideID") != null) {
//                        riderID = map.get("riderRideID").toString();
//                        getAssignedRiderPickupLocation();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//    }

//    public void getAssignedRiderPickupLocation() {
//
//        DatabaseReference assignedRiderPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(customerID).child("1");
//        assignedRiderPickupLocationRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()) {
//                    List<Object> map = (List<Object>) dataSnapshot.getValue();
//                    }
//                }
//            });
//
//    }

}
