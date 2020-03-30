package com.example.android.arrival.Dialogs;


import androidx.annotation.NonNull;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.android.arrival.Model.Car;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.List;
import java.util.Map;
/**
 * an alert dialog appears on the rider's screen when they click on Driver Details button
 * the alert dialog shows all the information of their driver (name, phone number, car details, driver rating etc)
 * the rider can contact the driver directly from the dialog view
 */

public class DriverDetailsFragment extends DialogFragment {
    private static final String TAG = "driver_fragment";

    Button phoneDriver;
    String driverPhone;

    public static DriverDetailsFragment newInstance(Driver driver, String driverID) {
        //Bundles the parameters to be passed along later
        Bundle args = new Bundle();
        args.putSerializable("driver", driver);
        args.putSerializable("driverID", driverID);

        DriverDetailsFragment fragment = new DriverDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_driver_profile, null);


        //Finding textviews in dialog_driver_profile to display driver details
        TextView name = view.findViewById(R.id.driver_name);
        TextView rating = view.findViewById(R.id.driver_rating);
        TextView time = view.findViewById(R.id.arrival_time);
        TextView car = view.findViewById(R.id.car_details);
        TextView plate = view.findViewById(R.id.license_plate);
        TextView phone = view.findViewById(R.id.phone);
        TextView email = view.findViewById(R.id.driver_email);
        phoneDriver = view.findViewById(R.id.phone_driver);


        //Gets the arguments from the bundle
        final Bundle args = getArguments();

        //If the arguments were not null then set the fields of the fragment to the values in args
        if (args != null) {
            Driver driver = (Driver) args.getSerializable("driver");
            String driverID = (String) args.getSerializable("driverID");

            String driverName = driver.getName();
            //String driverRating = driver.getRating();
            Car driverCar = driver.getCar();
            String vehicleDetails = driverCar.getYear() +  driverCar.getColor() +  driverCar.getMake() + driverCar.getModel();
            String licensePlate = driverCar.getPlate();
            driverPhone = driver.getPhoneNumber();
            String driverEmail = driver.getEmail();


            name.setText(driverName);
            rating.setText("This driver has no ratings.");
            //time.setText(estTime);
            car.setText(vehicleDetails);
            plate.setText(licensePlate);
            phone.setText(driverPhone);
            email.setText(driverEmail);

            FirebaseFirestore.getInstance().collection("ratings").whereEqualTo("driverID", driverID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> ds = queryDocumentSnapshots.getDocuments();

                            int up = 0;
                            int down = 0;
                            for(DocumentSnapshot s : ds) {
                                Log.d("Rating", ds.toString());
                                Log.d("Rating", s.get("rating").toString());

                                long r = (long) s.get("rating");
                                if(r == 1) {
                                    up ++;
                                } else {
                                    down ++;
                                }
                            }
                            if(ds.size() > 0) {
                                float ratio = ((float) up) / ds.size();
                                rating.setText("" + (ratio * 100) + "%");
                            } else {
                                rating.setText("This driver has no ratings.");
                            }
                        }
                    });
        }

        phoneDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked on phone number");
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel",driverPhone, null));
                startActivity(intent);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Driver Details")
                .setNegativeButton("Back to Map", null)
                .setPositiveButton("Cancel Rider", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO give riders the option to cancel a ride
                    }}).create();
    }
}
