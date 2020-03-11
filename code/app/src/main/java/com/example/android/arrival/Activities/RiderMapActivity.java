package com.example.android.arrival.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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


//TODO Create a new pin when we click on item in search list view
//TODO Create search views for start and default
//TODO Set default start location to be current location


public class RiderMapActivity extends FragmentActivity implements OnMapReadyCallback {
    //Declaring variables for use later
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private Marker currentUserLocationMarker;
    private static final int REQUEST_USER_LOCATION_CODE = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;

    //Variables for search suggestions
    ListView list;
    ArrayAdapter<Place> adapter;
    SearchView searchBar;
    ArrayList<Place> arrayList = new ArrayList<>();
    private int codeCall = 0;


    /**
     * When activity is initially called we set up some basic location items needed later
     *
     * @param savedInstanceState : saved state of the application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rider_map_activity);
        list = (ListView) findViewById(R.id.suggestionsListView);
        list.setVisibility(View.GONE);

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
        list.setVisibility(View.GONE);


        SearchView search = findViewById(R.id.searchButton);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * When a user submits the text from the searchView this function is called
             * @param query : final text in the searchBar
             * @return : returns false when finished executing
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Place marker when user selects a query suggestion
                list.setVisibility(View.GONE);
                return false;
            }

            /**
             * When a user updates the text in the searchView this function is called
             * @param newText : updated text in the searchBar
             * @return : returns false when finished executing
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                //list.setVisibility(View.VISIBLE);
                getSearch(newText);
                return false;
            }
        });

        //If the user clicks the x in the searchView it will hide the textView again
        search.setOnCloseListener(new SearchView.OnCloseListener(){
            public boolean onClose() {
                list.setVisibility(View.GONE);
                return false;
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place selected = (Place) parent.getItemAtPosition(position);

                LatLng latLng = new LatLng(selected.getLat(), selected.getLon());
                MarkerOptions markerOptions= new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.draggable(true);
                markerOptions.title("Destination");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                Marker destinationMarker = mMap.addMarker(markerOptions);


            }
        });
    }


    /**
     * Gets a list of possible addresses based on the users updated search
     * @param newText : new string in the searchBar
     */
    public void getSearch(String newText) {
        //Update suggestions when query text changes
        Geocoder geocoder = new Geocoder(getApplicationContext());
        searchBar = findViewById(R.id.searchButton);

        adapter = new CustomSuggestionList(getApplicationContext(), arrayList);
        list.setAdapter(adapter);
        list.setVisibility(View.VISIBLE);

        List<Address> addresses;
        try {
            //Gets addresses from within Edmonton
            addresses = geocoder.getFromLocationName(newText, 5,
                    53.431898,  -113.662692,
                    53.646464,  -113.343030);

            //If the geocoder found possible addresses we should add them to the
            //arrayList if they aren't already there
            if (addresses.size() > 0) {
                updateArraylist(addresses);
                adapter.notifyDataSetChanged();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * For each address that the geocoder found we check create a new place object and check
     * if it can be added to the arrayList
     * @param addresses : list of potential addresses determined by the geocoder
     */
    public void updateArraylist(List<Address> addresses){
        for (int i = 0; i < addresses.size(); i++) {
            String name = addresses.get(i).getFeatureName();
            String address = addresses.get(i).getAddressLine(0);
            Double lat = addresses.get(i).getLatitude();
            Double lon = addresses.get(i).getLongitude();
            Place place = new Place(name, address, lat, lon);

            //By default we add the first address that the geocoder found
            if (arrayList.size() == 0) {
                arrayList.add(0, place);
            }
            else {
                checkArrayList(place);
            }
        }

        //Keeps the suggestions to a max of 6 and filters out the older suggestions
        while(arrayList.size() > 6) {
            arrayList.remove(6);
        }
    }


    /**
     * For each Place we want to ensure that we aren't having duplicates in our arrayList so we
     * check that with this function
     * @param place : Place object
     */
    public void checkArrayList(Place place) {
        int a = arrayList.size() - 1;
        int count = 0;
        while(a >= 0) {
            count = count + place.compareTo(arrayList.get(a));
            a--;
        }
        if (count == arrayList.size()) {
            arrayList.add(0, place);
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

//                //Removes the old marker so we can update it
//                if (currentUserLocationMarker != null) {
//                    currentUserLocationMarker.remove();
//                }

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
                        if (codeCall == 0){
                            EditText startLocationText = findViewById(R.id.startLocation);
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
                Log.i("add", "Checking address with geocoder");
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
        Log.i("add", "no location from geocoder");
        return null;
    }


    /**
     * NOT SURE IF THIS IS NEEDED CAN POSSIBLY DELETE IT LATER (MAYBE ONLY FOR DRIVER?)
     * Gets the current position of the user
     *
     * @return : the location of the users current position
     */
//    public LatLng getMarkerLocation() {
//        return currentUserLocationMarker.getPosition();
//    }


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


