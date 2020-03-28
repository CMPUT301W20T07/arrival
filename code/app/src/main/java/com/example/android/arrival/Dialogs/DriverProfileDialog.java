package com.example.android.arrival.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.android.arrival.Model.Car;
import com.example.android.arrival.R;

public class DriverProfileDialog extends DialogFragment {
    private TextView name;
    private TextView rating;
    private TextView arrivalTime;
    private TextView carDetails;
    private TextView licensePlate;
    private TextView phoneNum;
    private TextView email;

    private TextView nameHint;
    private TextView ratingHint;
    private TextView arrivalTimeHint;
    private TextView carDetailsHint;
    private TextView licensePlateHint;
    private TextView phoneNumHint;
    private TextView emailHint;



    private DriverProfileDialog.OnFragmentInteractionListener listener;
    //Car result;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_driver_profile, null);

        // view binding

        nameHint = view.findViewById(R.id.name_hint);
        ratingHint = view.findViewById(R.id.rating_hint);
        arrivalTimeHint = view.findViewById(R.id.arrival_time_hint);
        carDetailsHint = view.findViewById(R.id.car_details_hint);
        licensePlateHint = view.findViewById(R.id.license_hint);
        phoneNumHint = view.findViewById(R.id.phone_hint);
        emailHint = view.findViewById(R.id.email_hint);

        name = view.findViewById(R.id.driver_name);
        rating = view.findViewById(R.id.driver_rating);
        arrivalTime = view.findViewById(R.id.arrival_time);
        carDetails = view.findViewById(R.id.car_details);
        licensePlate = view.findViewById(R.id.license_plate);
        phoneNum = view.findViewById(R.id.phone);
        email = view.findViewById(R.id.email);


        // new Car(make.getText().toString(), model.getText().toString(),
        //                                year.getText().toString(), plate.getText().toString(), color.getText().toString()


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Your Driver")
                .setNegativeButton("OK", null)
                .create();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof DriverProfileDialog.OnFragmentInteractionListener){
            listener = (DriverProfileDialog.OnFragmentInteractionListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + "must implement OnFragmentInteractionListener");
        }
    }


    public interface OnFragmentInteractionListener{
        void onDonePressed(Car s);
    }
}
