package com.example.android.arrival.Util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.android.arrival.Activities.RegistrationActivity;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountManager {

    public static final String TAG = "AccountManager: ";
    private static AccountManager instance;
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private CollectionReference userRef;
    private CollectionReference riderRef;
    private CollectionReference driverRef;

    public static AccountManager getInstance() {
        if(instance == null) {
            instance = new AccountManager();
        }
        return instance;
    }

    private AccountManager() {
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users");
        riderRef = db.collection("riders");
        driverRef = db.collection("drivers");

    }


    public void createDriverAccount(Driver driver, String password, Context context) {


        firebaseAuth.createUserWithEmailAndPassword(driver.getEmail(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = firebaseAuth.getCurrentUser().getUid();
                    DocumentReference usersDocReference = userRef.document(userId);

                    // add user type to look up table "users"
                    Map<String, String> user = new HashMap<>();
                    user.put("type", DRIVER_TYPE_STRING);
                    usersDocReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context,"You have been added as driver", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "There was a problem creating your account", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "User onFailure: " + e);
                            deleteDriverAccount();
                        }
                    });

                    // add user data to driver table
                    DocumentReference driverDocumentReference = driverRef.document(userId);
                    driverDocumentReference.set(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Your data has been stored", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "There was a problem creating your account", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Driver onFailure: " + e);
                            deleteDriverAccount();
                        }
                    });
                }
                else {
                    Toast.makeText(context, "There was a problem creating your account", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void createRiderAccount(Rider rider, String password, Context context) {

    }

    public void getAccountType() {


    }

    public void updateDriverInfo() {

    }

    public void updateRiderInfo() {

    }

    public void deleteDriverAccount() {

    }

    public void deleteRiderAccount() {

    }




}
