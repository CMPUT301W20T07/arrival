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

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "Registration";

    private FirebaseAuth firebaseAuth;

    private EditText txtName;
    private EditText txtPhoneNumber;
    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnDriverSignUp;
    private Button btnRiderSignUp;
    private FirebaseFirestore db;
    private CollectionReference riderRef;
    private CollectionReference driverRef;
    private CollectionReference userRef;
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

        // Initialize UI component references
        txtName = findViewById(R.id.user_name_editText);
        txtPhoneNumber = findViewById(R.id.user_phone_number_editText);
        txtEmail = findViewById(R.id.register_email_editText);
        txtPassword = findViewById(R.id.register_password_editText);
        btnDriverSignUp = findViewById(R.id.sign_up_driver);
        btnRiderSignUp = findViewById(R.id.sign_up_rider);

        // Set on click listeners
        btnDriverSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverSignup();
            }
        });

        btnRiderSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                riderSignup();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private void driverSignup() {

    }

    private void riderSignup() {
        String em = txtEmail.getText().toString();
        String pwd = txtPassword.getText().toString();
        db = FirebaseFirestore.getInstance();

        // Check for empty input
        if (em.isEmpty()) {
            txtEmail.setError("Please enter an email address");
        } else if (pwd.isEmpty()) {
            txtPassword.setError("Please enter a password ");
        } else if (!(em.isEmpty() && pwd.isEmpty())) {
            firebaseAuth.createUserWithEmailAndPassword(em, pwd)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String uName = txtName.getText().toString();
                                String uPhoneNumber = txtPhoneNumber.getText().toString();
                                storeRiderInDatabase(uName, uPhoneNumber, em);
                                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                            } else {
                                Log.d(TAG, task.getException().toString());
                                Toast.makeText(RegistrationActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * This function adds all rider data into firestore database
     * @param uName
     * @param uPhoneNumber
     * @param uEmail
     */
    public void storeRiderInDatabase(String uName, String uPhoneNumber, String uEmail) {

        userID = firebaseAuth.getCurrentUser().getUid();

        /*
         * handles inputting userId in users table along with their type
         */
        DocumentReference usersDocReference = db.collection("users").document(userID);
        Map<String, String> user = new HashMap<>();
        user.put("type", RIDER_TYPE_STRING);

        usersDocReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegistrationActivity.this, uName + "added as rider", Toast.LENGTH_SHORT).show();
            }
        });

        /*
        handles inputting users into rider table with all relevant data
         */
        DocumentReference ridersDocReference = db.collection("riders").document(userID);
        //Map<String, Object> rider = new HashMap<>();
        //rider.put("Name", uName);
        //rider.put("PhoneNumber", uPhoneNumber);
        //rider.put("Email", uEmail);

        Rider rider = new Rider(uEmail, uName, uPhoneNumber);

        ridersDocReference.set(rider).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegistrationActivity.this, uName + "data stored", Toast.LENGTH_SHORT).show();
            }
        });

    }
}

