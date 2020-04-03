package com.example.android.arrival.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.android.arrival.Model.Car;
import com.example.android.arrival.R;
<<<<<<< HEAD
/**
 * DialogFragment displayed when a user signs up as a driver
 * that prompts the user to input their car details
 */
=======
import com.google.firebase.auth.FirebaseAuth;

>>>>>>> master
public class CarDetailsDialog extends DialogFragment {

    private EditText make;
    private EditText model;
    private EditText year;
    private EditText color;
    private EditText plate;
    private OnFragmentInteractionListener listener;
    Car result;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_car_details, null);

        // view binding
        make = view.findViewById(R.id.edit_make);
        model = view.findViewById(R.id.edit_model);
        year = view.findViewById(R.id.edit_year);
        plate = view.findViewById(R.id.edit_plate);
        color = view.findViewById(R.id.edit_color);


        // new Car(make.getText().toString(), model.getText().toString(),
        //                                year.getText().toString(), plate.getText().toString(), color.getText().toString()


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Input Car Details")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String makeStr = make.getText().toString();
                        String modelStr = model.getText().toString();
                        String yearStr = year.getText().toString();
                        String plateStr = plate.getText().toString();
                        String colorStr = color.getText().toString();

                        if ((makeStr.isEmpty() || modelStr.isEmpty() || yearStr.isEmpty() || plateStr.isEmpty() || colorStr.isEmpty())) {
                            make.setError("Input all relevant info");
                        }
                        else {

                            result = new Car(makeStr, modelStr, yearStr, plateStr, colorStr);
                            listener.onDonePressed(result);
                        }
                    }
                }).create();



    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof CarDetailsDialog.OnFragmentInteractionListener){
            listener = (CarDetailsDialog.OnFragmentInteractionListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + "must implement OnFragmentInteractionListener");
        }
    }


    public interface OnFragmentInteractionListener{
        void onDonePressed(Car s);
    }
}
