package com.example.android.arrival.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
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
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.arrival.Model.CustomSuggestionList;
import com.example.android.arrival.Model.Place;
import com.example.android.arrival.R;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


//TODO get shared preferences working or some other method to save marker locations
//TODO get location selection when clicking on the map working
//TODO get a rideRequest confirmation fragment where user can edit the offer amount
//TODO get distance between 2 markers to calculate the estimated cost and time

public class RiderMapActivity extends FragmentActivity implements OnMapReadyCallback {
    //Declaring variables for use later
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private Marker currentUserLocationMarker;
    private Marker pickupMarker;
    private Marker destMarker;
    private Place pickup = new Place();
    private Place destination = new Place();
    private static final int REQUEST_USER_LOCATION_CODE = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private int codeCall = 0;
    private SharedPreferences storage;
    private SharedPreferences.Editor editor;
    private Map<String, ?> markers;


    /**
     * When activity is initially called we set up some basic location items needed later
     *
     * @param savedInstanceState : saved state of the application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rider_map_activity);

        storage = getPreferences(MODE_PRIVATE);
        markers = storage.getAll();
        editor = storage.edit();


        //Location service that can get a users location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * When the map is ready to use we set it up for our specifications
     *
     * @param googleMap : google maps object
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        EditText startLocationText = findViewById(R.id.startLocation);
        EditText endLocationText = findViewById(R.id.endLocation);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(60 * 1000); //Get updates every 60 seconds
        locationRequest.setFastestInterval(10 * 1000); //Get updates at fastest every 10 secs
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //Checking if user has previously allowed location permissions before
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "In if, permissions are granted");
                mMap.setMyLocationEnabled(true);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            } else {
                Log.d("Permissions", "In else, permissions are not granted");
                checkUserLocationPermission();
            }
        }

        getMarkers();

        //Adding pickup location if map is reloaded
        if (pickup.getLat() != null){
            Log.d("type", "Pickup location is available");
            LatLng latLng = new LatLng(pickup.getLat(), pickup.getLon());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.draggable(false);
            markerOptions.title("Pickup Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            startLocationText.setText(pickup.getAddress());
            pickupMarker = mMap.addMarker(markerOptions);
        }

        if(destination.getLat() != null) {
            Log.d("type", "Destination is available");
            LatLng latLng = new LatLng(destination.getLat(), destination.getLon());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.draggable(false);
            markerOptions.title("Destination");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            endLocationText.setText(destination.getAddress());
            destMarker = mMap.addMarker(markerOptions);
        }

        //Note start location activity is 1 and endlocation activity is 2
        startLocationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creates new instance of the fragment that we passed variables to
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                DialogFragment fragment = SearchFragment.newInstance(1);
                fragmentTransaction.add(0, fragment);
                fragmentTransaction.commit();
            }
        });


        //Note start location activity is 1 and endlocation activity is 2
        endLocationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creates new instance of the fragment that we passed variables to
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                DialogFragment fragment = SearchFragment.newInstance(2);
                fragmentTransaction.add(0, fragment);
                fragmentTransaction.commit();
            }
        });

        //This section of code will run when searchFragment sends over a place
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("selected");
        if (args != null) {
            addMarker(args);
        }
    }


    public void getMarkers() {
        for (Map.Entry<String, ?> entry : markers.entrySet()) {
            String pickupStr = storage.getString(entry.getKey(), "");
            String[] split = pickupStr.split("\\*");
            if (split[0].equals("")){
                break;
            }
            int type = Integer.parseInt(split[0]);
            Log.d("type", "Type: " + type);

            if (type == 1){
                pickup.setAddress(split[1]);
                pickup.setName(split[2]);
                pickup.setLat(Double.valueOf(split[3]));
                pickup.setLon(Double.valueOf(split[4]));
            }
            else{
                destination.setAddress(split[1]);
                destination.setName(split[2]);
                destination.setLat(Double.valueOf(split[3]));
                destination.setLon(Double.valueOf(split[4]));
            }

        }
    }

    public void addMarker(Bundle args) {
        //Gets place and activity type from the bundle
        Place selected = (Place) args.getSerializable("place");
        int activityType = args.getInt("type");

        LatLng latLng = new LatLng(selected.getLat(), selected.getLon());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(false);

        if (activityType == 2) {
            if (destMarker != null){
                destMarker.remove();
                destination = selected;
            }

            String marker = activityType + "*" + selected.getAddress()  + "*" + selected.getName()
                    + "*" + selected.getLon() + "*" + selected.getLat();
            editor.remove("destination");
            editor.putString("destination", marker);
            editor.apply();

            markerOptions.title("Destination");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            EditText endLocationText = findViewById(R.id.endLocation);
            endLocationText.setText(selected.getAddress());
            destMarker = mMap.addMarker(markerOptions);

        }
        else{
            if (pickupMarker != null){
                pickupMarker.remove();
                pickup = selected;
            }
            String marker = activityType + "*" + selected.getAddress()  + "*" + selected.getName()
                    + "*" + selected.getLat() + "*" + selected.getLon();
            editor.remove("pickup");
            editor.putString("pickup", marker);
            editor.apply();

            markerOptions.title("Pickup Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            Log.d("marker", "Setting start location text");
            EditText startLocationText = findViewById(R.id.startLocation);
            startLocationText.setText(selected.getAddress());
            pickupMarker = mMap.addMarker(markerOptions);
        }
    }


    /**
     * Sets the current location marker when new location information is available
     */
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            double diffX = 0;
            double diffY = 0;
            for (Location location : locationResult.getLocations()) {
                //lastLocation = location;
                assert mMap != null;
                mMap.setMyLocationEnabled(true);


                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();

                if (mMap != null) {
                    //Specifications for the current location marker
                    markerOptions.position(latLng);
                    markerOptions.draggable(true);
                    markerOptions.title("Current Location");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                    //Checking if the users location has changed drastically
                    if (currentUserLocationMarker != null) {
                        diffX = Math.abs(currentUserLocationMarker.getPosition().latitude -
                                location.getLatitude());
                        diffY = Math.abs(currentUserLocationMarker.getPosition().longitude -
                                location.getLongitude());
                    }

                    //Allows for slight variances in the location of the user
                    if (currentUserLocationMarker != null && diffX < 1 && diffY < 1) {
                        return;
                    }
                    //If the users location has changed we put a new marker and move the camera
                    else {
                        if (currentUserLocationMarker != null) {
                            currentUserLocationMarker.remove();
                        }
                        currentUserLocationMarker = mMap.addMarker(markerOptions);
                        Log.i("add", "current user location has been set");
                        Address currentLocation = getCurrentLocationAddress();
                        if (pickup == null && currentLocation != null){
                            EditText startLocationText = findViewById(R.id.startLocation);
                            Log.d("marker", "changing start location text. Code call: " + codeCall);
                            startLocationText.setText(currentLocation.getAddressLine(0));
                            codeCall = 1;
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                }
            }
        }
    };


    public Address getCurrentLocationAddress(){
        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> addresses = null;
        try {
            if (currentUserLocationMarker != null) {
                addresses = geocoder.getFromLocation(currentUserLocationMarker.getPosition().latitude,
                        currentUserLocationMarker.getPosition().longitude, 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses != null) {
            Log.i("add", addresses.get(0).getAddressLine(0));
            Address currentLocation = addresses.get(0);
            return currentLocation;
        }
        return null;
    }




    /**
     * Checks if the user has allowed for location permissions. If not it will prompt the user.
     */
    public void checkUserLocationPermission() {
        //If permission is not granted the app will ask for user permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Creates the alert dialog box that asks a user for permission to access their location
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(RiderMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
            }
        }
    }


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
                    Toast.makeText(this, "Permission Denied...", Toast.LENGTH_SHORT).show();
                }
        }
    }
}


