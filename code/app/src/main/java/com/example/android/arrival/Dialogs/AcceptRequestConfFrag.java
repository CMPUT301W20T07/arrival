package com.example.android.arrival.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.android.arrival.Model.Notification;
import com.example.android.arrival.Model.Place;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.example.android.arrival.Util.RequestCallbackListener;
import com.example.android.arrival.Util.RequestManager;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
//Show current request info, this is different for riders and drivers. Rider side contains
//driver name, while driver side contains distance from driver to rider.

/**
 * DialogFragment displayed when a Driver wants to accept a request.
 * Displays request details and information.
 */
public class AcceptRequestConfFrag extends DialogFragment {
    private String TAG  = "acceptRequest";

    private Request currRequest;
    private Marker marker;
    private String driverUID;

    private FirebaseFirestore fb;
    private RequestManager rm;

    private Context context;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.accept_request_fragment, null);
        DecimalFormat format = new DecimalFormat("0.00");

        super.onCreate(savedInstanceState);

        Log.d("Request", "Creating accept dialog");

        fb = FirebaseFirestore.getInstance();
        rm = RequestManager.getInstance();

        context = getContext();

        TextView custName = view.findViewById(R.id.custNameValue);
        TextView custDestination = view.findViewById(R.id.custDestinationValue);
        TextView distanceToCust = view.findViewById(R.id.distanceToCust);
        TextView distanceToCustVal = view.findViewById(R.id.distanceToCustValue);
        TextView custDistanceToDestination = view.findViewById(R.id.custDistanceToDestinationValue);
        TextView estTime = view.findViewById(R.id.estTimeValue);
        TextView custPaymentOffer = view.findViewById(R.id.custPaymentOfferValue);
        TextView driverName = view.findViewById(R.id.driverName);
        TextView driverNameVal = view.findViewById(R.id.driverNameValue);


        String userType = (String) getArguments().getSerializable("userType");
        currRequest = (Request) getArguments().getSerializable("currentRequest");

        if (userType.equals("driver")) {
            //Info passed from DriverMapActivity
            ArrayList<Marker> markers = (ArrayList<Marker>) getArguments().getSerializable("markerLocation");
            driverUID = (String) getArguments().getSerializable("driverUID");
            double driverLat = (double) getArguments().getSerializable("driverLat");
            double driverLon = (double) getArguments().getSerializable("driverLon");
            marker = markers.get(0);
            double distanceFromDriverToLocation = distance(currRequest.getStartLocation().getLat(), driverLat, currRequest.getStartLocation().getLon(), driverLon);
            distanceToCustVal.setText(format.format((int) distanceFromDriverToLocation));
            distanceToCust.setVisibility(View.VISIBLE);
            distanceToCustVal.setVisibility(View.VISIBLE);
            driverName.setVisibility(View.INVISIBLE);
            driverNameVal.setVisibility(View.INVISIBLE);
        }

        if (userType.equals("rider"))
        {
            driverName.setVisibility(View.VISIBLE);
            driverNameVal.setVisibility(View.VISIBLE);
            distanceToCust.setVisibility(View.INVISIBLE);
            distanceToCustVal.setVisibility(View.INVISIBLE);
            DocumentReference documentReference = fb.collection("drivers")
                    .document(currRequest.getDriver());
            documentReference.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            driverNameVal.setText((String) documentSnapshot.get("name"));
                        }
                    });
        }

        if (currRequest == null) {
            return null;
        }
        else {
                Place startLocation = currRequest.getStartLocation();
                Place endLocation = currRequest.getEndLocation();
                Float fare = currRequest.getFare();

                DocumentReference documentReference = fb.collection("riders").document(currRequest.getRider());
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        custName.setText((String) documentSnapshot.get("name"));
                    }
                });

                custDestination.setText(endLocation.getAddress());

                double distanceFromRiderStartToEnd = distance(startLocation.getLat(), endLocation.getLat(), startLocation.getLon(), endLocation.getLon());
                custDistanceToDestination.setText(format.format((int) distanceFromRiderStartToEnd));

                estTime.setText(format.format((int) (distanceFromRiderStartToEnd / 180)));

                custPaymentOffer.setText(format.format(fare));

        }

        if (currRequest.getStatus() == 0 && userType.equals("driver")) {
            distanceToCust.setVisibility(View.VISIBLE);
            distanceToCustVal.setVisibility(View.VISIBLE);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            return builder
                    .setView(view)
                    .setTitle("Accept Ride Request")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("AcceptRequestFrag", "OK clicked");
                            currRequest.setStatus(Request.ACCEPTED);
                            currRequest.setDriver(driverUID);
                            rm.updateRequest(currRequest, (RequestCallbackListener) getContext());

                            getRiderToken();
                        }
                    }).create();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            return builder
                    .setView(view)
                    .setTitle("Current Request")
                    .setNegativeButton("Close", null)
                    .create();
        }
    }

    public void getRiderToken() {
        Log.d("Notifications", "In getRiderToken");
        String uid = currRequest.getRider();
        final String[] token = new String[1];

        DocumentReference documentReference = fb.collection("riders").document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Rider riderData = documentSnapshot.toObject(Rider.class);
                token[0] = riderData.getTokenId();
                Log.d("Notifications", "Token in getRiderToken: " + token[0]);
                notifyRider(token[0]);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Notifications", "get data onFailure: " + e.toString());
            }
        });
    }

    public void notifyRider(String riderToken) {
        try {
            Log.d("Notification", "Rider Token: " + riderToken);

            if (riderToken != null) {
                Notification notification = new Notification(context, riderToken,
                        "Ride Request Status Update", "A driver has accepted your request");
                notification.sendNotification();
            }
        } catch (Exception e) {

        }
    }

    //GeeksforGeeks by Twinkl Bajaj (https://auth.geeksforgeeks.org/user/Twinkl%20Bajaj/articles)
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

}