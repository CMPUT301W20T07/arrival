package com.example.android.arrival.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LauncherActivity;
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
import android.view.Gravity;
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
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.arrival.Model.CustomSuggestionList;
import com.example.android.arrival.Model.Place;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.RequestCallbackListener;
import com.example.android.arrival.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


//TODO get a rideRequest confirmation fragment where user can edit the offer amount
//TODO get distance between 2 markers to calculate the estimated cost and time
//TODO send the RideRequest to the firebase (Might need Reilly for this) get it all packaged though

public class RiderMapActivity extends FragmentActivity implements OnMapReadyCallback, RequestCallbackListener {

    private static final String TAG = "RiderMapActivity";

    //Declaring variables for use later
    private GoogleMap mMap;
    private LocationRequest locationRequest;

    private Marker currentUserLocationMarker;
    private Marker pickupMarker;
    private Marker destMarker;
    private Place pickup = new Place();
    private Place destination = new Place();
    private Place current = new Place();

    private static final int REQUEST_USER_LOCATION_CODE = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;

    //ArrayList that holds the markers so we can add them again when the map reloads
    private ArrayList<Place> marks = new ArrayList<>();

    private Request currentRequest;



    /**
     * When activity is initially called we set up some basic location items needed later
     *
     * @param savedInstanceState : saved state of the application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rider_map_activity);

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
        Button rideRequest = findViewById(R.id.requestRide);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(60 * 1000); //Get updates every 60 seconds
        locationRequest.setFastestInterval(10 * 1000); //Get updates at fastest every 10 secs
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //Checking if user has previously allowed location permissions before
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            } else {
                checkUserLocationPermission();
            }
        }


//        //Adding pickup location if map is reloaded
//        if (pickup.getLat() != null){
//            LatLng latLng = new LatLng(pickup.getLat(), pickup.getLon());
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(latLng);
//            markerOptions.draggable(false);
//            markerOptions.title("Pickup Location");
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//            startLocationText.setText(pickup.getAddress());
//            pickupMarker = mMap.addMarker(markerOptions);
//        }
//
//        //Adding destination location if the map is reloaded
//        if(destination.getLat() != null) {
//            LatLng latLng = new LatLng(destination.getLat(), destination.getLon());
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(latLng);
//            markerOptions.draggable(false);
//            markerOptions.title("Destination");
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//            endLocationText.setText(destination.getAddress());
//            destMarker = mMap.addMarker(markerOptions);
//        }

        //Adding pickup location if map is reloaded
        if (pickup.getLatLng() != null){
            LatLng latLng = pickup.getLatLng();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.draggable(false);
            markerOptions.title("Pickup Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            startLocationText.setText(pickup.getAddress());
            pickupMarker = mMap.addMarker(markerOptions);
        }

        //Adding destination location if the map is reloaded
//        if(destination.getLat() != null) {
//            LatLng latLng = new LatLng(destination.getLat(), destination.getLon());
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(latLng);
//            markerOptions.draggable(false);
//            markerOptions.title("Destination");
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//            endLocationText.setText(destination.getAddress());
//            destMarker = mMap.addMarker(markerOptions);
//        }

        //Adding destination location if the map is reloaded
        if(destination.getLatLng() != null) {
            LatLng latLng = destination.getLatLng();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.draggable(false);
            markerOptions.title("Destination");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            endLocationText.setText(destination.getAddress());
            destMarker = mMap.addMarker(markerOptions);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("map", "map was clicked");
                View customView = getLayoutInflater().inflate(R.layout.pickup_dest_popup, null);

                PopupWindow popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                popupWindow.showAtLocation(customView, Gravity.TOP, 0, 0);

                Button pickupActivity = customView.findViewById(R.id.pickupButton);
                Button destActivity = customView.findViewById(R.id.destButton);
                Button back = customView.findViewById(R.id.back);

                pickupActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (pickupMarker != null){
                            pickupMarker.remove();
                        }
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.draggable(false);
                        markerOptions.title("Pickup Location");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        pickupMarker = mMap.addMarker(markerOptions);
                        popupWindow.dismiss();
                        Address pickupAddress = getCurrentLocationAddress(pickupMarker);

                        pickup.setName(pickupAddress.getFeatureName());
                        pickup.setAddress(pickupAddress.getAddressLine(0));
//                        pickup.setLat(pickupAddress.getLatitude());
//                        pickup.setLon(pickupAddress.getLongitude());
                        pickup.setLatLng(pickupAddress.getLatitude(), pickupAddress.getLongitude());
                        startLocationText.setText(pickup.getAddress());

                        marks.set(0, pickup);
                    }
                });

                destActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (destMarker != null) {
                            destMarker.remove();
                        }
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.draggable(false);
                        markerOptions.title("Destination");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        destMarker = mMap.addMarker(markerOptions);
                        popupWindow.dismiss();
                        Address pickupAddress = getCurrentLocationAddress(destMarker);

                        destination.setName(pickupAddress.getFeatureName());
                        destination.setAddress(pickupAddress.getAddressLine(0));
                        destination.setLatLng(pickupAddress.getLatitude(), pickupAddress.getLongitude());
                        endLocationText.setText(destination.getAddress());

                        marks.add(1, destination);
                    }
                });

                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

            }
        });

        //Note pickup activity is 1 and destination activity is 2
        startLocationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creates new instance of the search fragment that we pass variables to
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                DialogFragment fragment = SearchFragment.newInstance(1, marks);
                fragmentTransaction.add(0, fragment);
                fragmentTransaction.commit();
            }
        });



        //Note pickup activity is 1 and destination activity is 2
        endLocationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creates new instance of the search fragment that we pass variables to
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                DialogFragment fragment = SearchFragment.newInstance(2, marks);
                fragmentTransaction.add(0, fragment);
                fragmentTransaction.commit();
            }
        });

        rideRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickup != null && destination != null) {
                    //TODO go to new intent to confirm RideRequest and get estimate cost and distance
                    //Creates new instance of the RideRequest fragment that we pass variables to
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    DialogFragment fragment = RideRequestConfFrag.newInstance(marks);
                    fragmentTransaction.add(0, fragment);
                    fragmentTransaction.commit();
                }
            }
        });

        //This section of code will run when searchFragment sends over a place
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("selected");
        if (args != null) {
            addMarker(args);
        }
    }


    /**
     * Gets the bundle sent over by the SearchFragment and adds/updates the markers on the screen
     * @param args : bundle sent over by the SearchFragment
     */
    public void addMarker(Bundle args) {
        //Gets place, activity type and marks from the bundle
        Place selected = (Place) args.getSerializable("place");
        int activityType = args.getInt("type");
        marks = (ArrayList<Place>) args.getSerializable("marks");

        LatLng latLng = selected.getLatLng();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(false);

        //If the search was for a destination
        if (activityType == 2) {
            //Deletes the old destination marker if necessary
            if (destMarker != null){
                destMarker.remove();
            }
            destination = selected;
            marks.add(1, destination);

            markerOptions.title("Destination");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            EditText endLocationText = findViewById(R.id.endLocation);
            endLocationText.setText(selected.getAddress());
            destMarker = mMap.addMarker(markerOptions);

            //Checks if we need to add a pickup marker that may have been erased when the map reloaded
            addPickupMarker();

        }
        //If the search was for a pickup
        else{
            //Deletes the old pickup marker if necessary
            if (pickupMarker != null){
                pickupMarker.remove();
            }
            pickup = selected;
            marks.set(0, pickup);

            markerOptions.title("Pickup Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            EditText startLocationText = findViewById(R.id.startLocation);
            startLocationText.setText(selected.getAddress());
            pickupMarker = mMap.addMarker(markerOptions);

            //Checks if we need to add a destination marker that may have been erased when the map relaoded
            addDestMarker();
        }
    }

    /**
     * Adds a pickup marker to the screen
     */
    public void addPickupMarker() {
        if (marks.get(0) != null){
            LatLng latLng = marks.get(0).getLatLng();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.draggable(false);
            markerOptions.title("Pickup Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            EditText startLocationText = findViewById(R.id.startLocation);
            startLocationText.setText(marks.get(0).getAddress());
            pickupMarker = mMap.addMarker(markerOptions);
        }
    }


    /**
     * Adds a destination marker to the screen
     */
    public void addDestMarker(){
        if (marks.size() > 1) {
            LatLng latLng = marks.get(1).getLatLng();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.draggable(false);
            markerOptions.title("Destination");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            EditText endLocationText = findViewById(R.id.endLocation);
            endLocationText.setText(marks.get(1).getAddress());
            destMarker = mMap.addMarker(markerOptions);
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
                        Address currentLocation = getCurrentLocationAddress(currentUserLocationMarker);
                        //If this is the first time the map is loaded set the default pickup
                        //location to be the current location
                        if (marks.size() == 0){
                            EditText startLocationText = findViewById(R.id.startLocation);
                            startLocationText.setText(currentLocation.getAddressLine(0));

//                            current.setLon(currentLocation.getLongitude());
//                            current.setLat(currentLocation.getLatitude());
                            current.setLatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            current.setAddress(currentLocation.getAddressLine(0));
                            current.setName(currentLocation.getFeatureName());

                            marks.add(0, current);
                            addPickupMarker();
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                }
            }
        }
    };


    /**
     * Uses a geocoder to the get the address from a marker
     * @return : an Address object associated with the currentUserLocationMarker
     */
    public Address getCurrentLocationAddress(Marker marker){
        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> addresses = null;
        try {
            if (marker != null) {
                addresses = geocoder.getFromLocation(marker.getPosition().latitude,
                        marker.getPosition().longitude, 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses != null) {
            Log.i("add", addresses.get(0).getAddressLine(0));
            return addresses.get(0);
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
                        .setTitle("Current Location Permission Denied")
                        .setMessage("Please give access to current location.")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
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
                //If permission is accepted then create map with user location
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                    mMap.setMyLocationEnabled(true);
                }
                //else show permission denied message
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onCallbackStart() {
        // Load...
    }

    @Override
    public void update() {
        Log.d(TAG, "Opened new request. ");
    }

    @Override
    public void onGetRequestSuccess(DocumentSnapshot snapshot) {
        Request req = snapshot.toObject(Request.class);
        currentRequest = req;
        Log.d(TAG, "Retrieved request: " + req.toString());
    }

    @Override
    public void onGetOpenSuccess(QuerySnapshot snapshot) {
        // Convert the snapshot to objects that can be used to display information
        List<Request> openRequests = snapshot.toObjects(Request.class);
//        requestAdapter.clear();
//        requestAdapter.addAll(openRequests);
//        requestAdapter.notifyDataSetChanged();
        Log.d(TAG, "getOpen() : " + openRequests.toString());
    }

    @Override
    public void onGetRiderRequestsSuccess(QuerySnapshot snapshot) {

    }

    @Override
    public void onGetDriverRequestsSuccess(QuerySnapshot snapshot) {

    }
}


