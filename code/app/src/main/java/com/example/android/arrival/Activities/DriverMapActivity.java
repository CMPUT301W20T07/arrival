package com.example.android.arrival.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.example.android.arrival.Dialogs.AcceptRequestConfFrag;
import com.example.android.arrival.Dialogs.DriverReviewFragment;
import com.example.android.arrival.Dialogs.ScanQRDialog;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.example.android.arrival.Util.AccountCallbackListener;
import com.example.android.arrival.Util.AccountManager;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Android Activity that displays the Driver's Map. Allows Drivers
 * to accept requests, pick up riders, drop them off, and receive payment
 */
public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback, RequestCallbackListener, ScanQRDialog.OnFragmentInteractionListener, AccountCallbackListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "DriverMapActivity";
    private static final int CAMERA_REQUEST = 100;
    private static final int REFRESH_INTERVAL = 10; // How often the application should auto refresh in seconds

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private static final int REQUEST_USER_LOCATION_CODE = 99;
    //Youtube video by SimCoder https://www.youtube.com/watch?v=u10ZEnARZag&t=857s
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean zoom = true;
    private LatLng placedMarkerLocation;

    private String driverUID;
    private String accountType;
    private Driver mydriverObject;
    static boolean currentActivity = false;
    private int index;

    ArrayList<Request> requestsList = new ArrayList<>();

    private FirebaseFirestore fb;
    private RequestManager rm;
    private AccountManager accountManager;

    private Handler handler;
    private Request currRequest;
    private Button btnCancelRide;
    private Button btnConfirmPickup;
    private Button btnCompleteRide;
    private Button btnScanQR;
    private Button btnRemoveMarker;
    private Button btnConfirmPayment;
    private FloatingActionButton btnRefresh;
    private TextView txtStatus;
    private Toolbar toolbarDriver;
    private DrawerLayout drawerDriver;
    private TextView userName;
    private TextView userEmailAddress;
    private CircularImageView profilePhoto;
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    public void onBackPressed() {
        if(drawerDriver.isDrawerOpen(GravityCompat.START)){
            drawerDriver.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountManager = AccountManager.getInstance();
        driverUID = accountManager.getUID();
        accountManager.getProfilePhoto(this, driverUID);
        accountManager.getUserData(this);
        accountManager.getAccountType(driverUID, DriverMapActivity.this);
        setContentView(R.layout.driver_map_activity);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fb = FirebaseFirestore.getInstance();
        rm = RequestManager.getInstance();

        // Bind components
        btnCancelRide = findViewById(R.id.driverCancelRide);
        btnConfirmPickup = findViewById(R.id.driverConfirmPickup);
        btnCompleteRide = findViewById(R.id.driverCompleteRide);
        btnScanQR = findViewById(R.id.btnDriverScanQR);
        btnRefresh = findViewById(R.id.btnDriverRefresh);
        txtStatus = findViewById(R.id.txtDriverStatus);
        toolbarDriver = findViewById(R.id.toolbar3);
        drawerDriver = findViewById(R.id.driver_navigation_layout);
        btnRemoveMarker = findViewById(R.id.removeMarker);
        btnConfirmPayment = findViewById(R.id.driverConfirmPayment);

        // Runtime Handler
        handler = new Handler();

        //Handling Navigation Drawer
        NavigationView navigationView = findViewById(R.id.driver_navigation_view);
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.userNameDriver);
        userEmailAddress = headerView.findViewById(R.id.userEmailAddressDriver);
        profilePhoto = headerView.findViewById(R.id.user_profile_pic_driver);

        //Setting up Persistent Bottom Sheet
        View bottomSheet = findViewById(R.id.driver_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(140);
        bottomSheetBehavior.setHideable(false);

        //Setting Navigation View click listener
        navigationView.setNavigationItemSelectedListener(this);

        toolbarDriver.setBackgroundColor(Color.TRANSPARENT);
        setSupportActionBar(toolbarDriver);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle newToggle = new ActionBarDrawerToggle(this, drawerDriver, toolbarDriver, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerDriver.addDrawerListener(newToggle);
        newToggle.syncState();

        Window currentWindow = this.getWindow();
        currentWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        currentWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapActivity.this, DriverProfileScreenActivity.class);
                intent.putExtra("driver", mydriverObject);
                startActivity(intent);
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
                placedMarkerLocation = null;
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

        btnConfirmPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Payment completed.");
                currRequest.setStatus(Request.COMPLETED);
                rm.updateRequest(currRequest, (RequestCallbackListener) v.getContext());
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
    }

    /**
     * Refreshes the app the every REFRESH_INTERVAL seconds
     */
    Runnable runner =  new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(runner, REFRESH_INTERVAL * 1000);
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reviews:
                Log.d(TAG, "starting review list frag...");
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                DialogFragment fragment = DriverReviewFragment.newInstance();
                fragmentTransaction.add(0, fragment);
                fragmentTransaction.commit();
                break;
            case R.id.ride_history_driver:
                if (currRequest != null) {
                    Toast.makeText(this, "Cannot view past rides while driving", Toast.LENGTH_SHORT).show();
                    break;
                }
                else {
                    rm.getDriverRequests(driverUID, this);
                    break;
                }
            case R.id.sign_out_button_driver:
                Log.d(TAG, "btnSignOut Clicked");
                Log.d(TAG, "Attempting to sign out user... ");
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
        }
        return true;
    }

    /**
     * Fetches information relative to the current user and current request
     */
    public void refresh() {
        Log.d(TAG, "refreshing...");
        if(currRequest!=null) {
            rm.getRequest(currRequest.getID(), this);
        }
    }


    /**
     * Updates the UI relative the current request's status
     */
    public void updateInfo() {
        Log.d(TAG, "Updating info...");
        if (currRequest == null) {
            rm.getOpenRequests(this);

            btnCancelRide.setVisibility(View.INVISIBLE);
            btnConfirmPickup.setVisibility(View.INVISIBLE);
            btnCompleteRide.setVisibility(View.INVISIBLE);
            btnScanQR.setVisibility(View.INVISIBLE);
            btnConfirmPayment.setVisibility(View.INVISIBLE);
            txtStatus.setText("");
        } else {
            Log.d(TAG, "currRequest is " + currRequest.toString());

            txtStatus.setText(Request.STATUS.get(currRequest.getStatus()));

            requestsList.clear();


            if(currRequest.getStatus() == Request.OPEN) {
                currRequest = null;
            } else if (currRequest.getStatus() == Request.ACCEPTED) {
                //Clear open requests from map, except currRequest
                mMap.clear();
                MarkerOptions mop = new MarkerOptions();
                mop.position(currRequest.getStartLocation().getLatLng());
                mMap.addMarker(mop);

                btnCancelRide.setVisibility(View.VISIBLE);
                btnConfirmPickup.setVisibility(View.VISIBLE);
                btnCompleteRide.setVisibility(View.INVISIBLE);
                btnScanQR.setVisibility(View.INVISIBLE);
                btnConfirmPayment.setVisibility(View.INVISIBLE);
                btnRemoveMarker.setVisibility(View.INVISIBLE);
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
                btnConfirmPayment.setVisibility(View.INVISIBLE);
                btnRemoveMarker.setVisibility(View.INVISIBLE);

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
                btnConfirmPayment.setVisibility(View.VISIBLE);
                btnRemoveMarker.setVisibility(View.INVISIBLE);

            } else if (currRequest.getStatus() == Request.COMPLETED) {
                rm.getOpenRequests(this);
                btnCancelRide.setVisibility(View.INVISIBLE);
                btnConfirmPickup.setVisibility(View.INVISIBLE);
                btnCompleteRide.setVisibility(View.INVISIBLE);
                btnScanQR.setVisibility(View.INVISIBLE);
                btnConfirmPayment.setVisibility(View.INVISIBLE);
                btnRemoveMarker.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Check for camera permissions to scan QR code
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermissions(Context context){
        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }
    }

    /**
     * Open the qr scanner
     */
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

        findRequestsByLocation();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onMarkerClick(Marker marker) {
                checkPermissions(getApplicationContext());
                //Share marker and requests with the fragment
                    for (int i = 0; i < requestsList.size(); i++) {
                        if (requestsList.get(i).getStartLocation().getLat() == marker.getPosition().latitude &&
                                requestsList.get(i).getStartLocation().getLon() == marker.getPosition().longitude) {
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
                    args.putSerializable("driverUID", driverUID);
                    args.putSerializable("driverLat", currentLocation.getLatitude());
                    args.putSerializable("driverLon", currentLocation.getLongitude());
                    args.putSerializable("userType", accountType);

                    AcceptRequestConfFrag acceptRequestConfFrag = new AcceptRequestConfFrag();
                    acceptRequestConfFrag.setArguments(args);
                    fragmentTransaction.add(0, acceptRequestConfFrag);
                    fragmentTransaction.commit();

                return false;
            }
        });
    }

    /**
     * finding requests by pressing an area on the map to see all available requests within 2km
     */
    public void findRequestsByLocation() {
        if (currRequest == null) {
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    placedMarkerLocation = latLng;
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    mMap.clear();
                    rm.getOpenRequests(DriverMapActivity.this);
                    btnRemoveMarker.setVisibility(View.VISIBLE);
                    btnRemoveMarker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "btnRemoveMarker clicked");
                            placedMarkerLocation = null;
                            mMap.clear();
                            rm.getOpenRequests(DriverMapActivity.this);
                            btnCancelRide.setVisibility(View.INVISIBLE);
                            btnCompleteRide.setVisibility(View.INVISIBLE);
                            btnConfirmPickup.setVisibility(View.INVISIBLE);
                            btnScanQR.setVisibility(View.INVISIBLE);
                            btnRemoveMarker.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            });
        }
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
                    getCurrentRequest();
                    if(currRequest == null)
                    {
                        updateInfo();
                    }
                    updateDriverPosition();
                }
            }
        }
    };

    /**
     * Check for current requests when the app is opened to make sure the driver
     * does not have more than one current request or to retrieve the request
     * if a crash happens
     */
    public void getCurrentRequest(){
        fb.collectionGroup("requests")
                .whereEqualTo("driver", driverUID)
                .whereIn("status", Arrays.asList(1, 2, 3))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                queryDocumentSnapshots.toObjects(Request.class);
                if (queryDocumentSnapshots.size() != 0) {
                    currRequest = (Request) queryDocumentSnapshots.toObjects(Request.class).get(0);
                    if (currRequest.getStatus() == 1) {
                        MarkerOptions markerOptions = new MarkerOptions();

                        markerOptions.position(currRequest.getStartLocation().getLatLng());
                        mMap.addMarker(markerOptions);
                        updateInfo();
                    }
                    if (currRequest.getStatus() == 2 || currRequest.getStatus() == 3) {
                        MarkerOptions markerOptions = new MarkerOptions();

                        markerOptions.position(currRequest.getEndLocation().getLatLng());
                        mMap.addMarker(markerOptions);
                        updateInfo();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Get Current Request");
            }
        });
    }

    public void updateDriverPosition(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Map<String, Object> driverInfo = new HashMap<>();
            driverInfo.put("lat", currentLocation.getLatitude());
            driverInfo.put("lon", currentLocation.getLongitude());

            fb.collection("availableDrivers").document(driverUID)
                    .update(driverInfo)
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
    }

    /**
     * When map is closed stop showing the driver's location making the driver unavailable
     */
    @Override
    protected void onStop() {
        super.onStop();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Map<String, Object> driverInfo = new HashMap<>();
            driverInfo.put("lat", null);
            driverInfo.put("lon", null);

            fb.collection("availableDrivers").document(driverUID)
                    .update(driverInfo)
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
    }

    /**
     * Check if the map activity is open
     */
    //Stackoverflow post by virsir https://stackoverflow.com/users/238061/virsir
    // "How to check if my activity is the current activity running in the screen"
    // Answer https://stackoverflow.com/questions/3262157/how-to-check-if-my-activity-is-the-current-activity-running-in-the-screen
    @Override
    public void onResume() {
        super.onResume();
        currentActivity = true;

        runner.run();
    }

    /**
     * Check if the map activity is closed
     */
    @Override
    public void onPause() {
        super.onPause();
        currentActivity = false;

        handler.removeCallbacks(runner);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateDriverPosition();
        }
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

    /**
     * Get the distance between 2 points
     * @param lat1
     * @param lat2
     * @param lon1
     * @param lon2
     * @return
     */
    // GeeksforGeeks by Twinkl Bajaj (https://auth.geeksforgeeks.org/user/Twinkl%20Bajaj/articles)
    // "Program for distance between two points on earth"
    // https://www.geeksforgeeks.org/program-distance-two-points-earth/
    public static double distance(double lat1, double lat2, double lon1, double lon2) {

        // The math module contains a function named toRadians which converts from degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers.
        double r = 6371;

        // calculate the result
        return(c * r);
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

        if(req.getStatus() == Request.CANCELLED || req.getStatus() == Request.COMPLETED) {
            currRequest = null;
        } else {
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

        if (placedMarkerLocation == null) {
            for (int i = 0; i < requestsList.size(); i++)
            {
                Request request = requestsList.get(i);
                double distanceToPickUp = distance(currentLocation.getLatitude(), request.getStartLocation().getLat(),
                        currentLocation.getLongitude(), request.getStartLocation().getLon());

            if(distanceToPickUp <= 2.0)
            {
                MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.position(request.getStartLocation().getLatLng());
                mMap.addMarker(markerOptions);
            }
            }
        } else {
            for (int i = 0; i < requestsList.size(); i++) {
                Request request = requestsList.get(i);
                double markerRadius = distance(placedMarkerLocation.latitude, request.getStartLocation().getLat(),
                        placedMarkerLocation.longitude, request.getStartLocation().getLon());

                if (markerRadius <= 2.0) {
                    MarkerOptions markerOptions = new MarkerOptions();

                    markerOptions.position(request.getStartLocation().getLatLng());
                    mMap.addMarker(markerOptions);
                }
            }
        }
    }

    @Override
    public void onGetRiderRequestsSuccess(QuerySnapshot snapshot) {

    }

    @Override
    public void onGetRiderOpenRequestsSuccess(QuerySnapshot snapshot) {

    }

    @Override
    public void onGetDriverRequestsSuccess(QuerySnapshot snapshot) {

        ArrayList<Request> requests= new ArrayList<>();

        for (QueryDocumentSnapshot document : snapshot) {
            Request request = document.toObject(Request.class);
            Log.d(TAG, "onGetDriverRequestsSuccess: " + request.getDriver());
            requests.add(request);
        }
        if (!requests.isEmpty()) {
            Intent intent = new Intent(DriverMapActivity.this, RideHistoryActivity.class);
            intent.putExtra("list", requests);
            intent.putExtra("accountType", AccountManager.DRIVER_TYPE_STRING);
            startActivity(intent);
        }
        else {
            Log.d(TAG, "onGetDriverRequestsSuccess: no requests found");
            Toast.makeText(this, "You have no requests", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onDonePressed(String s) {
        currRequest.setStatus(Request.COMPLETED);
        rm.updateRequest(currRequest, (RequestCallbackListener) DriverMapActivity.this);
        refresh();
    }

    @Override
    public void onAccountTypeRetrieved(String userType) {
        accountType = userType;

    }

    @Override
    public void onAccountTypeRetrieveFailure(String e) {

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
        mydriverObject = driver;
        userName.setText(driver.getName());
        userEmailAddress.setText(driver.getEmail());
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
        Toast.makeText(this, e,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAccountUpdated() {

    }

    @Override
    public void onAccountUpdateFailure(String e) {

    }
}







