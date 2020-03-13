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

public class AcceptRequestConfFrag extends DialogFragment {
    private Place pickup = new Place();
    private Place destination = new Place();
    private Request requestInfo = new Request();
    int index;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.accept_request_fragment, null);
        DecimalFormat format = new DecimalFormat("0.00");

        super.onCreate(savedInstanceState);

        TextView custName = view.findViewById(R.id.custNameValue);
        TextView custDestination = view.findViewById(R.id.custDestinationValue);
        TextView distanceToCust = view.findViewById(R.id.distanceToCustValue);
        TextView custDistanceToDestination = view.findViewById(R.id.custDistanceToDestinationValue);
        TextView estTime = view.findViewById(R.id.estTimeValue);
        TextView custPaymentOffer = view.findViewById(R.id.custPaymentOfferValue);

        ArrayList<Request> requestArrayList = (ArrayList<Request>)getArguments().getSerializable("requestsList");
        ArrayList<Marker> markers = (ArrayList<Marker>)getArguments().getSerializable("markerLocation");
        Marker marker = markers.get(0);

        LatLng latlng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);

        for (int i = 0; i < requestArrayList.size(); i++) {
            if ((requestArrayList.get(i).getStartLocation().getLatLng()) == latlng){

                index = i;

            }
        }

        if (!requestArrayList.isEmpty()) {

            String riderName = requestArrayList.get(index).getRider();
            Place startLocation = requestArrayList.get(index).getStartLocation();
            Place endLocation = requestArrayList.get(index).getEndLocation();
            Float fare = requestArrayList.get(index).getFare();


            custName.setText(riderName);
            custDestination.setText(endLocation.getAddress());

            firebaseFirestore.collection("availableDrivers").document("driver1")
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    double driverLat = (double) documentSnapshot.get("lat");
                    double driverLon = (double) documentSnapshot.get("lat");
                    LatLng driverLatLng = new LatLng(driverLat, driverLon);
                    double distance1 = distance(startLocation.getLat(), driverLat, startLocation.getLon(), driverLon);
                    distanceToCust.setText(format.format((int) distance1));
                }
            });

            double distance2 = distance(startLocation.getLat(), endLocation.getLat(), startLocation.getLon(), endLocation.getLon());
            custDistanceToDestination.setText(format.format((int) distance2));

            double recTime = distance2;
            estTime.setText(format.format((int) (recTime / 180)));

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
                        //TODO send the data to the request manager
                        //lat and lon stored in pick and destination
                        //get cost from the yourOfferValue textbox
//                        requestsList.indexOf();

                    }}).create();
    }

    //TODO need to cite this
    // https://www.geeksforgeeks.org/program-distance-two-points-earth/
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
