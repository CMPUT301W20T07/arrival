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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.android.arrival.Model.Place;
import com.example.android.arrival.Model.Request;
import com.example.android.arrival.Model.RequestCallbackListener;
import com.example.android.arrival.Model.RequestManager;
import com.example.android.arrival.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class RideRequestConfFrag extends DialogFragment {

    private static final String TAG = "RideRequestFrag";

    private RequestManager rm;

    private Place pickup = new Place();
    private Place destination = new Place();

    static RideRequestConfFrag newInstance(ArrayList<Place> marks){
        //Bundles the parameters to be passed along later
        Bundle args = new Bundle();
        args.putSerializable("marks", marks);

        RideRequestConfFrag fragment = new RideRequestConfFrag();
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ArrayList<Place> marks = new ArrayList<>();
        DecimalFormat format = new DecimalFormat("0.00");

        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.ride_request_fragment, null);

        // Get instance of Request Manager
        rm = RequestManager.getInstance();

        // Get component references
        TextView pickupLoc = view.findViewById(R.id.pickupLoc);
        TextView destLoc = view.findViewById(R.id.destLoc);
        TextView distanceValue = view.findViewById(R.id.distanceValue);
        TextView recCostValue = view.findViewById(R.id.recCostValue);
        EditText yourOfferValue = view.findViewById(R.id.yourOfferValue);

        //Gets the arguments from the bundle
        final Bundle args = getArguments();

        //If the arguments were not null then set the fields of the fragment to the values in args
        if (args != null) {
            marks= (ArrayList<Place>) args.getSerializable("marks");
            pickup = marks.get(0);
            destination = marks.get(1);

            pickupLoc.setText(pickup.getAddress());
            destLoc.setText(destination.getAddress());

            double distance = distance(pickup.getLat(), destination.getLat(), pickup.getLon(), destination.getLon());
            distanceValue.setText(format.format(distance));

            double recCost = 5 + distance;
            recCostValue.setText(format.format(recCost));

            yourOfferValue.setText(format.format(recCost));

        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Confirm Ride Request")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Pressed OK");
                        float currFare = 0;
                        if(yourOfferValue.getText().length() == 0){
                            Toast.makeText(getContext(), "Fare offer cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                        else if(Float.parseFloat(yourOfferValue.getText().toString()) <
                                Float.parseFloat(recCostValue.getText().toString())){
                            Toast.makeText(getContext(), "Offer must be at least the recommended",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            currFare = Float.parseFloat(yourOfferValue.getText().toString());
                            Request req = new Request("usr-map-test", pickup, destination, currFare);
                            rm.openRequest(req, (RequestCallbackListener) getContext());
                        }

                    }}).create();
    }

    //TODO need to cite this
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
