package com.example.android.arrival.Activities;


import androidx.annotation.NonNull;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.android.arrival.Model.Car;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;



public class DriverDetailsFragment extends DialogFragment {
    private static final String TAG = "driver_fragment";

    String phoneNum = "7801234567";
    TextView phoneDriver;
    String driverPhone;

    static DriverDetailsFragment newInstance(Driver driver) {
        //Bundles the parameters to be passed along later
        Bundle args = new Bundle();
        args.putSerializable("driver", driver);

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

            String driverName = driver.getName();
            //String driverRating = driver.getRating();
            Car driverCar = driver.getCar();
            String vehicleDetails = driverCar.getYear() +  driverCar.getColor() +  driverCar.getMake() + driverCar.getModel();
            String licensePlate = driverCar.getPlate();
            driverPhone = driver.getPhoneNumber();
            String driverEmail = driver.getEmail();


            name.setText(driverName);
            //rating.setText(driverRating);
            //time.setText(estTime);
            car.setText(vehicleDetails);
            plate.setText(licensePlate);
            phone.setText(driverPhone);
            email.setText(driverEmail);
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
