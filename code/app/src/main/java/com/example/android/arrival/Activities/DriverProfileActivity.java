package com.example.android.arrival.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.arrival.Dialogs.CarDetailsDialog;
import com.example.android.arrival.Dialogs.DriverProfileDialog;
import com.example.android.arrival.Model.Car;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.Model.User;
import com.example.android.arrival.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public abstract class DriverProfileActivity extends AppCompatActivity implements DriverProfileDialog.OnFragmentInteractionListener {

    private static final String TAG = "Registration";

    private FirebaseAuth firebaseAuth;
    private EditText txtName;
    private EditText txtPhoneNumber;
    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnDriverSignUp;
    private Button btnRiderSignUp;
    private Car driverCar;
    private FirebaseFirestore db;
    private String userID;
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);

        // Initialize connection to Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();


//        DriverProfileDialog driverDetailsDialog = new DriverProfileDialog();
//        driverDetailsDialog.show(getSupportFragmentManager(), "driverProfile");
    }

}