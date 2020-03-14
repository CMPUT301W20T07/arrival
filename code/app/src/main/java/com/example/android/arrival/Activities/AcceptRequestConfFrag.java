package com.example.android.arrival.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.android.arrival.Model.Place;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.RequestCallbackListener;
import com.example.android.arrival.Model.RequestManager;
import com.example.android.arrival.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//Add open request information to fragment

public class AcceptRequestConfFrag extends DialogFragment {

    private Place pickup = new Place();
    private Place destination = new Place();
    private Request requestInfo = new Request();
    private Request currRequest;
    private Marker marker;

    private int index;

    private FirebaseFirestore fb;
    private RequestManager rm;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.accept_request_fragment, null);
        DecimalFormat format = new DecimalFormat("0.00");

        super.onCreate(savedInstanceState);

        fb = FirebaseFirestore.getInstance();
        rm = RequestManager.getInstance();

        TextView custName = view.findViewById(R.id.custNameValue);
        TextView custDestination = view.findViewById(R.id.custDestinationValue);
        TextView distanceToCust = view.findViewById(R.id.distanceToCustValue);
        TextView custDistanceToDestination = view.findViewById(R.id.custDistanceToDestinationValue);
        TextView estTime = view.findViewById(R.id.estTimeValue);
        TextView custPaymentOffer = view.findViewById(R.id.custPaymentOfferValue);

        //Info passed from DriverMapActivity
        ArrayList<Marker> markers = (ArrayList<Marker>)getArguments().getSerializable("markerLocation");
        currRequest = (Request) getArguments().getSerializable("currentRequest");

        assert currRequest != null;

        marker = markers.get(0);
        if (currRequest.getStartLocation().getLat() == marker.getPosition().latitude && currRequest.getStartLocation().getLon() == marker.getPosition().longitude) {

            String riderName = currRequest.getRider();
            Place startLocation = currRequest.getStartLocation();
            Place endLocation = currRequest.getEndLocation();
            Float fare = currRequest.getFare();


            custName.setText(riderName);
            custDestination.setText(endLocation.getAddress());

            //Get the drivers location to calculate the distance from the marker selected
            fb.collection("availableDrivers").document("driver1")
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    double driverLat = (double) documentSnapshot.get("lat");
                    double driverLon = (double) documentSnapshot.get("lon");
                    double distance1 = distance(startLocation.getLat(), driverLat, startLocation.getLon(), driverLon);
                    distanceToCust.setText(format.format((int) distance1));
                }
            });

            double distance2 = distance(startLocation.getLat(), endLocation.getLat(), startLocation.getLon(), endLocation.getLon());
            custDistanceToDestination.setText(format.format((int) distance2));

            double recTime = distance2;
            estTime.setText(format.format((int) (distance2 / 180)));

            custPaymentOffer.setText(format.format(fare));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Accept Ride Request")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("AcceptRequestFrag", "OK clicked");
                        currRequest.setStatus(Request.STATUS_ACCEPTED);
                        currRequest.setDriver("curr-driver");
                        rm.updateRequest(currRequest, (RequestCallbackListener) getContext());
                    }}).create();
    }

    //GeeksforGeeks by Twinkl Bajaj, Program for distance between two points on earth, https://www.geeksforgeeks.org/program-distance-two-points-earth/
    public static double distance(double lat1, double lat2, double lon1, double lon2) {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
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

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        return(c * r);
    }

}