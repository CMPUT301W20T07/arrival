package com.example.android.arrival.Activities;


import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.android.arrival.Dialogs.DisplayQRDialog;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Place;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.example.android.arrival.Util.AccountCallbackListener;
import com.example.android.arrival.Util.AccountManager;
import com.example.android.arrival.Model.Notification;
import com.example.android.arrival.Util.RequestCallbackListener;
import com.example.android.arrival.Util.RequestManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.ValueEventListener;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//import com.google.android.gms.common.api.GoogleApiClient;


public class RiderMapActivity extends AppCompatActivity implements OnMapReadyCallback, AccountCallbackListener,
        RequestCallbackListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "RiderMapActivity";
    private static final int REFRESH_INTERVAL = 1000 * 45; // 45 seconds in millis

    private RequestManager rm;
    private DrawerLayout drawer;

//    private AlarmManager am;
    private Handler handler;

    //Declaring variables for use later
    private GoogleMap mMap;
    private LocationRequest locationRequest;

    private Marker currentUserLocationMarker;
    private Marker pickupMarker;
    private Marker destMarker;
    private Place pickup = new Place();
    private Place destination = new Place();
    private Place current = new Place();

    private Polyline line;

    private static final int REQUEST_USER_LOCATION_CODE = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;

    //ArrayList that holds the markers so we can add them again when the map reloads
    private ArrayList<Place> marks = new ArrayList<>();

    private Request currRequest;

    private EditText txtStartLocation;
    private EditText txtEndLocation;
    private Button btnRequestRide;
    private Button btnCancelRide;
    private Button btnPayment;
    private TextView txtStatus;
    private FloatingActionButton btnRefresh;
    private Toolbar toolbar2;
    private AccountManager accountManager;
    private TextView userName;
    private TextView userEmailAddress;
    private ImageView profilePhoto;

//     private Button btnPayment;
////     private TextView txtStatus;
////     private FloatingActionButton btnRefresh;
////     private Toolbar toolbar2;
//     private AccountManager accountManager;
//     private TextView userName;
//     private TextView userEmailAddress;
//     private ImageView profilePhoto;


    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }


    private FirebaseFirestore db;
    //private ValueEventListener postListener;

    //final FirebaseDatabase database = null;

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

        rm = RequestManager.getInstance();
//        am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        handler = new Handler();
        accountManager = AccountManager.getInstance();

        //txtStartLocation = findViewById(R.id.pickupLocation);
//        txtEndLocation = findViewById(R.id.destLocation);
//        btnRequestRide = findViewById(R.id.requestRide);
//        btnCancelRide = findViewById(R.id.cancelRide);
//        btnPayment = findViewById(R.id.btnRiderShowQR);
        btnRefresh = findViewById(R.id.btnRiderRefresh);
//        txtStatus = findViewById(R.id.txtRiderStatus);
        toolbar2 = findViewById(R.id.toolbar2);
        drawer = findViewById(R.id.rider_drawer_layout);
        NavigationView navigationView = findViewById(R.id.rider_navigation_view);
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.userName);
        userEmailAddress = headerView.findViewById(R.id.userEmailAddress);
        profilePhoto = headerView.findViewById(R.id.user_profile_pic);


        //Setting Navigation View click listener
        navigationView.setNavigationItemSelectedListener(this);

        //Set transparent toolbar
        toolbar2.setBackgroundColor(Color.TRANSPARENT);
        setSupportActionBar(toolbar2);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle newToggle = new ActionBarDrawerToggle(this, drawer, toolbar2, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(newToggle);
        newToggle.syncState();

        Window currentWindow = this.getWindow();
        currentWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        currentWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        accountManager.getUserData(this);
        accountManager.getProfilePhoto(this);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
                String TOKENJess = "e57Wwg5MRW2HGzdCwvqdGp:APA91bGll5e4Xr1FtTXvwmSOGtt9Q815n6L8322Lbab7LaJ8b2_tcRqAO9Y88Ngw8gHFqqAN2a0ojoJTvbiZ63FCuIpULUFWFFC3PbDjPT5wgxJRW2G4jvNc5moN8jpgThG_fxhoWacX";
                //String TOKENNana = "eYU7QB_OTyiu5fii2lC1aR:APA91bEZ8YAKFvsR0uUO5u5n-th81uUiblHO-_hojb0Ym7ZQg6-hHlhtxwoBNy-6vzHbqnW7Kx7amyzisITdXzZqxzDpsXYzNxGDYXVk869iKzJBE_Lb9jCcFuk5MuLGJr4e5K4608jk";
                Notification notification = new Notification(getApplicationContext(), TOKENJess,
                        "Ride Request Status Update", "A driver has accepted your request");
                notification.sendNotification();
            }
        });

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RiderMapActivity.this, RiderProfileScreenActivity.class));
            }
        });

        updateInfo();
    }

    private Runnable periodicUpdate = new Runnable() {
        @Override
        public void run() {
            if(handler!= null) {
                handler.postDelayed(periodicUpdate, REFRESH_INTERVAL);
//                Log.d(TAG, "Handler update...");
                refresh();
            } else {
                handler = new Handler();
            }
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_button:
                Log.d(TAG, "btnSignOut Clicked");
                Log.d(TAG, "Attempting to sign out user... ");
                //FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(RiderMapActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;

        }
        return true;
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
             rm.getRequest("1", this);
        }
    }

    public void updateInfo() {

        if(mMap == null) {
            Log.d(TAG, "null map");
            return;
        }

        if (currRequest == null) {
            txtStatus.setText("");

            mMap.clear();
            addPickupMarker(pickup.getLatLng());
            addDestMarker(destination.getLatLng());

            btnRequestRide.setVisibility(View.VISIBLE);
            btnCancelRide.setVisibility(View.INVISIBLE);
            //btnPayment.setVisibility(View.INVISIBLE);

        } else {
            Log.d(TAG, "currRequest is " + currRequest.toString());
            txtStatus.setText(Request.STATUS.get(currRequest.getStatus()));

            if (currRequest.getStatus() == Request.OPEN) {
                mMap.clear();
                addPickupMarker(currRequest.getStartLocation().getLatLng());
                addDestMarker(currRequest.getEndLocation().getLatLng());

                btnRequestRide.setVisibility(View.INVISIBLE);
                btnCancelRide.setVisibility(View.VISIBLE);
                //btnPayment.setVisibility(View.INVISIBLE);

            } else if (currRequest.getStatus() == Request.PICKED_UP) {
                addPickupMarker(currRequest.getStartLocation().getLatLng());
                mMap.clear();
                addDestMarker(currRequest.getEndLocation().getLatLng());

                btnRequestRide.setVisibility(View.INVISIBLE);
                btnCancelRide.setVisibility(View.INVISIBLE);
                //btnPayment.setVisibility(View.INVISIBLE);

            } else if(currRequest.getStatus() == Request.AWAITING_PAYMENT) {
                btnRequestRide.setVisibility(View.INVISIBLE);
                btnCancelRide.setVisibility(View.INVISIBLE);
                //btnPayment.setVisibility(View.VISIBLE);

            } else if(currRequest.getStatus() == Request.COMPLETED) {
                    mMap.clear();
                    currRequest = null;
                    btnRequestRide.setVisibility(View.VISIBLE);
                    btnCancelRide.setVisibility(View.INVISIBLE);
                    //btnPayment.setVisibility(View.INVISIBLE);
                    txtEndLocation.setText("");
            }
        }
    }




    /**
     * Called when the map resumes from its previous state
     */
    @Override
    public void onResume() {
        super.onResume();
//        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + REFRESH_INTERVAL, REFRESH_INTERVAL, refresh);

        refresh();
        periodicUpdate.run();
    }

    /**
     * Check if the map activity is closed
     */
    @Override
    public void onPause() {
        Log.d(TAG, "map on pause");
        super.onPause();
        //active = false;
        handler.removeCallbacks(periodicUpdate);
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
                mMap.setMyLocationEnabled(true);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            } else {
                checkUserLocationPermission();
            }
        }



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "map was clicked");
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
                        addPickupMarker(latLng);
                        popupWindow.dismiss();

                    }
                });

                destActivity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (destMarker != null) {
                            destMarker.remove();
                        }
                        addDestMarker(latLng);
                        popupWindow.dismiss();
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
        txtStartLocation.setOnClickListener(new View.OnClickListener() {
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
        txtEndLocation.setOnClickListener(new View.OnClickListener() {
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

        btnRequestRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // assert(!txtStartLocation.getText().toString().equals("") && !txtEndLocation.getText().toString().equals(""));

                // Log.d(TAG, "btnRequestRide clicked");
                // if (pickup != null && destination != null) {
                if (pickupMarker != null && destMarker!= null) {
                    Log.d(TAG, "btnRequestRide clicked");
                    //Creates new instance of the RideRequest fragment that we pass variables to
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    DialogFragment fragment = RideRequestConfFrag.newInstance(marks);
                    fragmentTransaction.add(0, fragment);
                    fragmentTransaction.commit();
                }
                else{
                    Toast.makeText(RiderMapActivity.this,
                            "Please select pickup and destination locations", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnCancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currRequest.setStatus(Request.CANCELLED);
                rm.updateRequest(currRequest, (RequestCallbackListener) v.getContext());
            }
        });

//        btnPayment.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "Generating QR...");
//                // TODO: Indicate to the user that a QR payment is loading
//                FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
//                DisplayQRDialog displayQRDialog = DisplayQRDialog.newInstance(currRequest.generateID());
//                displayQRDialog.show(fm, "generate");
//            }
//        });

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

        if (selected == null){
            if (marks.get(1) != null){
                addDestMarker(marks.get(1).getLatLng());
            }
            if (marks.get(0) != null){
                addPickupMarker(marks.get(0).getLatLng());
            }

            return;
        }

        LatLng latLng = selected.getLatLng();

        //If the search was for a destination
        if (activityType == 2) {
            //Deletes the old destination marker if necessary
            if (destMarker != null){
                destMarker.remove();
            }
            addDestMarker(latLng);

            //Checks if we need to add a pickup marker that may have been erased when the map reloaded
            if(marks.get(0)!= null) {
                addPickupMarker(marks.get(0).getLatLng());
            }

        }
        //If the search was for a pickup
        else{
            if (pickupMarker != null){
                pickupMarker.remove();
            }
            addPickupMarker(latLng);
            if(marks.get(1) != null) {
                addDestMarker(marks.get(1).getLatLng());
            }
        }
    }


    /**
     * Adds a pickup marker to the map
     * @param latLng : set of lat/lon coordinates
     */
    public void addPickupMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(false);
        markerOptions.title("Pickup Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        pickupMarker = mMap.addMarker(markerOptions);

        Address pickupAddress = getCurrentLocationAddress(pickupMarker);
        pickup.setName(pickupAddress.getFeatureName());
        pickup.setAddress(pickupAddress.getAddressLine(0));
        pickup.setLatLng(pickupAddress.getLatitude(), pickupAddress.getLongitude());
        txtStartLocation.setText(pickup.getAddress());

        marks.set(0, pickup);

        addLine();
    }


    /**
     * adds a destination marker to the map
     * @param latLng : set of lat/lon coordinates
     */
    public void addDestMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(false);
        markerOptions.title("Destination");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        destMarker = mMap.addMarker(markerOptions);

        Address pickupAddress = getCurrentLocationAddress(destMarker);
        destination.setName(pickupAddress.getFeatureName());
        destination.setAddress(pickupAddress.getAddressLine(0));
        destination.setLatLng(pickupAddress.getLatitude(), pickupAddress.getLongitude());
        txtEndLocation.setText(destination.getAddress());

        if(marks.size() == 0 ) {
            marks.add(0, null);
            marks.add(1, destination);
        } else {
            marks.add(1, destination);
        }

        addLine();
    }


    public void addLine() {
        if (marks.size() >= 2){
            if (line != null ){
                line.remove();
            }
            line = mMap.addPolyline(new PolylineOptions()
                    .add(marks.get(0).getLatLng(), marks.get(1).getLatLng())
                    .width(10)
                    .color(Color.RED));
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
                    markerOptions.title("Current Location");
                    markerOptions.visible(false);

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
                        if (pickupMarker == null){
                            txtStartLocation.setText(currentLocation.getAddressLine(0));

                            current.setLatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            current.setAddress(currentLocation.getAddressLine(0));
                            current.setName(currentLocation.getFeatureName());

                            //pickupMarker = currentUserLocationMarker;

                            marks.add(0, current);
                            addPickupMarker(latLng);
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


    /**
     * Gets the results of the location permissions and acts accordingly
     * @param requestCode : int of what request we are calling
     * @param permissions : string of permissions granted
     * @param grantResults : integer array of results
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

        txtStatus.setText(Request.STATUS.get(req.getStatus()));

        Log.d(TAG, "Retrieved request: " + req.toString());

        if(req.getStatus() == Request.CANCELLED) {
            currRequest = null;
        } else if(currRequest == null || !currRequest.equals(req)) {
            currRequest = req;
        }

        updateInfo();
    }

    @Override
    public void onGetOpenSuccess(QuerySnapshot snapshot) {
        // Convert the snapshot to objects that can be used to display information
        List<Request> openRequests = snapshot.toObjects(Request.class);
        Log.d(TAG, "getOpen() : " + openRequests.toString());
    }

    @Override
    public void onGetRiderRequestsSuccess(QuerySnapshot snapshot) {

    }

    @Override
    public void onGetDriverRequestsSuccess(QuerySnapshot snapshot) {

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

        userName.setText(rider.getName());
        userEmailAddress.setText(rider.getEmail());

    }

    @Override
    public void onDriverDataRetrieved(Driver driver) {

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
        Glide.with(this).load(uri).into(profilePhoto);
        Log.d(TAG, "onPhotoReceived: " + uri.toString());
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