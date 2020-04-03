package com.example.android.arrival.Dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.android.arrival.Model.Car;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;
/**
 * an alert dialog appears on the rider's screen when they click on Driver Details button
 * the alert dialog shows all the information of their driver (name, phone number, car details, driver rating etc)
 * the rider can contact the driver directly from the dialog view
 */

public class DriverDetailsFragment extends DialogFragment {
    private static final String TAG = "driver_fragment";

    private Button phoneDriver;
    private String driverPhone;
    private FirebaseStorage firebaseStorage;

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
        firebaseStorage = FirebaseStorage.getInstance();

        //Finding textviews in dialog_driver_profile to display driver details
        TextView name = view.findViewById(R.id.driver_name);
        TextView rating = view.findViewById(R.id.driver_rating);
        TextView car = view.findViewById(R.id.driver_car_model);
        TextView plate = view.findViewById(R.id.driver_plate);
        TextView phone = view.findViewById(R.id.driver_dialog_phone);
        TextView email = view.findViewById(R.id.driver_email);
        phoneDriver = view.findViewById(R.id.phone_driver_button);
        CircularImageView imageView = view.findViewById(R.id.driver_profile_pic);


        //Gets the arguments from the bundle
        final Bundle args = getArguments();

        //If the arguments were not null then set the fields of the fragment to the values in args
        if (args != null) {
            Driver driver = (Driver) args.getSerializable("driver");
            String driverID = (String) args.getSerializable("driverID");

            String driverName = driver.getName();
            //String driverRating = driver.getRating();
            Car driverCar = driver.getCar();
            String vehicleDetails = driverCar.getYear() + " "+ driverCar.getColor() +" "+  driverCar.getMake() + " " + driverCar.getModel();
            String licensePlate = driverCar.getPlate();
            driverPhone = driver.getPhoneNumber();
            String driverEmail = driver.getEmail();


            name.setText(driverName);
            rating.setText(R.string.no_ratings_string);
            car.setText(vehicleDetails);
            plate.setText(licensePlate);
            phone.setText(driverPhone);
            email.setText(driverEmail);

            StorageReference storageReference = firebaseStorage.getReference().child("images/" + driverID);
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        Glide.with(DriverDetailsFragment.this).load(uri).into(imageView);
                    }
                    else {
                        Toast.makeText(getContext(), "Could not fetch photo for driver", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: -Get Photo" + task.getException().toString());
                    }
                }
            });

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
                                int ratio = (int) ((((float) up) / ds.size()) * 100);
                                rating.setText("" + ratio + "%");
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
                .create();
    }
}
