package com.example.android.arrival.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.Util.AccountCallbackListener;
import com.example.android.arrival.Util.AccountManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Android Activity displayed upon app startup. Authenticates
 * user and decides which activity to send them to.
 */
public class SplashActivity extends AppCompatActivity implements AccountCallbackListener {

    public static final String TAG = "SplashActivity";

    FirebaseAuth firebaseAuth;
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";
    AccountManager accountManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        accountManager = AccountManager.getInstance();
        //accountManager.getAccountType(user.getUid(), this);
        // Makes sure the account is still valid
        if(user != null) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // User still exits
                        Log.d(TAG, "User = " + user.getEmail());
                        accountManager.getAccountType(user.getUid(), SplashActivity.this);
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

    @Override
    public void onAccountTypeRetrieved(String userType) {
        if (userType.equals(DRIVER_TYPE_STRING)) {
            Intent intent = new Intent(SplashActivity.this, DriverMapActivity.class);
            startActivity(intent);
            finish();
        }
        else if (userType.equals(RIDER_TYPE_STRING)){
            Intent intent = new Intent(SplashActivity.this, RiderMapActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Toast.makeText(SplashActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccountTypeRetrieveFailure(String e) {
        Log.d(TAG, "onFailure: could not get document: " + e);
        Toast.makeText(SplashActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccountCreated(String accountType) {

    }

    @Override
    public void onAccountCreationFailure(String e) {

    }

    @Override
    public void onRiderDataRetrieved(Rider rider) {

    }

    @Override
    public void onDriverDataRetrieved(Driver driver) {

    }

    @Override
    public void onDataRetrieveFail(String e) {

    }

    @Override
    public void onAccountDeleted() {

    }

    @Override
    public void onAccountDeleteFailure(String e) {

    }

    @Override
    public void onImageUpload() {

    }

    @Override
    public void onImageUploadFailure(String e) {

    }

    @Override
    public void onPhotoReceived(Uri uri) {

    }

    @Override
    public void onPhotoReceiveFailure(String e) {

    }

    @Override
    public void onAccountUpdated() {

    }

    @Override
    public void onAccountUpdateFailure(String e) {

    }
}
