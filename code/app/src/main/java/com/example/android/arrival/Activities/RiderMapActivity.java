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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.arrival.Model.Place;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Util.RequestCallbackListener;
import com.example.android.arrival.Util.RequestManager;
import com.example.android.arrival.R;
import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class RiderMapActivity extends FragmentActivity implements OnMapReadyCallback, RequestCallbackListener {

    private static final String TAG = "RiderMapActivity";

    private RequestManager rm;

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
    private Button btnSignOut;
    private TextView txtStatus;
    private FloatingActionButton btnRefresh;

    private FirebaseFirestore db;
    private ValueEventListener postListener;

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

        txtStartLocation = findViewById(R.id.pickupLocation);
        txtEndLocation = findViewById(R.id.destLocation);
        btnRequestRide = findViewById(R.id.requestRide);
        btnCancelRide = findViewById(R.id.cancelRide);
        btnSignOut = findViewById(R.id.btnRiderSignout);
        btnRefresh = findViewById(R.id.btnRiderRefresh);
        txtStatus = findViewById(R.id.txtRiderStatus);

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btnSignOut Clicked");
                Log.d(TAG, "Attempting to sign out user... ");
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(RiderMapActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        if(currRequest == null) {
            btnRequestRide.setVisibility(View.VISIBLE);
            btnCancelRide.setVisibility(View.INVISIBLE);
        } else {
            btnRequestRide.setVisibility(View.INVISIBLE);
            btnCancelRide.setVisibility(View.VISIBLE);
        }


       /* //Get the drivers location to calculate the distance from the marker selected
        CollectionReference cr = db.collection("requests").document(String.valueOf(currentRequest)).collection("status");
        DataSnapshot ds = (DataSnapshot) cr.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.d(TAG, "something");
                    }
                });*/


       // save code
/*        db.collection("requests").document(String.valueOf(currentRequest)).collection("status")
                .whereEqualTo("status","0")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.d(TAG, "Listen failed:" + e);
                            return;
                        }
                        Log.d(TAG, "open request!");
                    }
                });*/

//        final DocumentReference docRef = db.collection("requests").document(String.valueOf(currentRequest));
//        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                if (e!= null) {
//                    Log.w(TAG, "Listen failed.", e);
//                    return;
//                }
//                if (documentSnapshot != null && documentSnapshot.exists()) {
//                    Log.d(TAG, "Current data: " +  documentSnapshot.getData());
//                }
//                else {
//                    Log.d(TAG, "Current data: null");
//                }
//            }
//        });

//        postListener = new ValueEventListener() {
//
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Post post = dataSnapshot.getValue(Post.class);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d(TAG, "loadPost:onCancelled", databaseError.toException());
//            }
//        };
//        db.addValueEventListener(postListener);


    }

    public void refresh() {
        Log.d(TAG, "refreshing...");
        if(currRequest!=null) {
            rm.getRequest(currRequest.getID(), this);
        }
    }

    public void updateInfo() {
        if (currRequest == null) {
            txtStatus.setText("");

            mMap.clear();
            addPickupMarker(pickup.getLatLng());
            addDestMarker(destination.getLatLng());

            btnRequestRide.setVisibility(View.VISIBLE);
            btnCancelRide.setVisibility(View.INVISIBLE);

        } else {
//            rm.getRequest(currRequest.getID(), this);

            Log.d(TAG, "currRequest is " + currRequest.toString());
            txtStatus.setText(Request.STATUS.get(currRequest.getStatus()));

            if (currRequest.getStatus() == Request.OPEN) {
                mMap.clear();
                addPickupMarker(currRequest.getStartLocation().getLatLng());
                addDestMarker(currRequest.getEndLocation().getLatLng());

                btnRequestRide.setVisibility(View.INVISIBLE);
                btnCancelRide.setVisibility(View.VISIBLE);

            } else if (currRequest.getStatus() == Request.PICKED_UP) {
                mMap.clear();
                addDestMarker(currRequest.getEndLocation().getLatLng());

                btnRequestRide.setVisibility(View.INVISIBLE);
                btnCancelRide.setVisibility(View.INVISIBLE);
            } else if(currRequest.getStatus() == Request.COMPLETED) {
                mMap.clear();
                currRequest = null;
                btnRequestRide.setVisibility(View.VISIBLE);
                btnCancelRide.setVisibility(View.INVISIBLE);
                txtEndLocation.setText("");
            }
        }
    }

    /*final DocumentReference docRef = (DocumentReference) db.collection("requests").document(String.valueOf(currentRequest))
            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                }
            });
*/


//    final DocumentReference docRef = db.collection("requests").document(String.valueOf(currentRequest));
//    docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//        @Override
//        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//            if (e!= null) {
//                Log.w(TAG, "Listen failed.", e);
//                return;
//            }
//            if (documentSnapshot != null && documentSnapshot.exists()) {
//                Log.d(TAG, "Current data: " +  documentSnapshot.getData());
//            }
//            else {
//                Log.d(TAG, "Current data: null");
//            }
//        }
//    });


    /**
     * Called when the map resumes from its previous state
     */
    @Override
    public void onResume() {
        super.onResume();
        if(mMap != null) {
            refresh();
        }
    }

    /**
     * Check if the map activity is closed
     */
    @Override
    public void onPause() {
        Log.d(TAG, "map on pause");
        super.onPause();
        //active = false;
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

        marks.add(1, destination);

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
//            refresh();
        } else if(currRequest == null || !currRequest.equals(req)) {
            currRequest = req;
//            refresh();
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
}