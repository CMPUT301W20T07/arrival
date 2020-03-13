package com.example.android.arrival.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.arrival.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    public static final String TAG = "SplashActivity";

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener firebaseAuthListener;
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        // Makes sure the account is still valid
        if(user != null) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // User still exits
                        Log.d(TAG, "User = " + user.getEmail());
                        checkUserType(user.getUid());
                    } else {
                        // User no longer exists, send to login screen
                        Log.d(TAG, "User is null.");
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        } else {
            Log.d(TAG, "User is null.");
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * checks user type
     * @param uid takes in userID to search the users document on firestore
     * @return userType
     */
    public void checkUserType(String uid){


        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection("users").document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String docData = documentSnapshot.get("type").toString();
                Log.d(TAG, "onSuccess: " + docData);
                if (docData.equals(DRIVER_TYPE_STRING)) {
                    Intent intent = new Intent(SplashActivity.this, DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if (docData.equals(RIDER_TYPE_STRING)){
                    Intent intent = new Intent(SplashActivity.this, RiderMapActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Log.d(TAG, "onComplete: " + uid + " " + docData);
                    Toast.makeText(SplashActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: could not get document: " + uid);
                        Toast.makeText(SplashActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
