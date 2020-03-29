package com.example.android.arrival.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.arrival.Dialogs.CarDetailsDialog;
import com.example.android.arrival.Model.Car;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.example.android.arrival.Util.AccountCallbackListener;
import com.example.android.arrival.Util.AccountManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class RegistrationActivity extends AppCompatActivity implements CarDetailsDialog.OnFragmentInteractionListener, AccountCallbackListener {

    private static final String TAG = "Registration";
    private static final int GALLERY_RC = 100;
    private static final int PHOTO_RC = 150;
    private EditText txtName;
    private EditText txtPhoneNumber;
    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnDriverSignUp;
    private Button btnRiderSignUp;
    private Car driverCar;
    private CircularImageView profileImage;
    private AccountManager accountManager;
    private Uri filePath = null;
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";
    private static String IMAGE_DIRECTORY = "Arrival";

    private String uTokenId = "";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);

        accountManager = AccountManager.getInstance();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        uTokenId = task.getResult().getToken();
                        Log.d(TAG, uTokenId);
                    }
                });

        // Initialize UI component references
        txtName = findViewById(R.id.user_name_editText);
        txtPhoneNumber = findViewById(R.id.user_phone_number_editText);
        txtEmail = findViewById(R.id.register_email_editText);
        txtPassword = findViewById(R.id.register_password_editText);
        btnDriverSignUp = findViewById(R.id.sign_up_driver);
        btnRiderSignUp = findViewById(R.id.sign_up_rider);
        profileImage = findViewById(R.id.profile_image);

        // Set on click listeners

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });
        btnDriverSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CarDetailsDialog carDetailsDialog = new CarDetailsDialog();
                carDetailsDialog.show(getSupportFragmentManager(), "car");
            }
        });

        btnRiderSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                riderSignUp(uTokenId);
            }
        });
    }

    // https://demonuts.com/pick-image-gallery-camera-android/
    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            chooseFromGallery();
                        }
                    }
                });
        pictureDialog.show();
    }


    private void chooseFromGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.setType("image/");
        startActivityForResult(gallery, GALLERY_RC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode);
        if (requestCode == GALLERY_RC &&data != null) {
            try {
                filePath = data.getData();
                Bitmap bitmap = null;
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "onActivityResult: " + e.toString());
            }

            Log.d(TAG, "onActivityResult: " + filePath.toString());

        }
    }

    public void riderSignUp(String uTokenId) {
        String em = txtEmail.getText().toString();
        String pwd = txtPassword.getText().toString();
        String uName = txtName.getText().toString();
        String uPhoneNumber = txtPhoneNumber.getText().toString();


        if (!Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
            txtEmail.setError("Please enter a valid email address");
        }
        // Check for empty input
        if (em.isEmpty()) {
            txtEmail.setError("Please enter an email address");
        }
        if (pwd.isEmpty()) {
            txtPassword.setError("Please enter a password ");
        }
        if (uName.isEmpty()) {
            txtName.setError("Please enter your name");
        }
        if (uPhoneNumber.isEmpty()) {
            txtPhoneNumber.setError("Please input your phoneNumber");
        }
        if (filePath == null) {
            Snackbar.make(profileImage, "Please input a photo", Snackbar.LENGTH_SHORT).show();
        }
        if (!(em.isEmpty() && pwd.isEmpty() && uName.isEmpty() && uPhoneNumber.isEmpty() && filePath == null)) {
            Rider rider = new Rider(em, uName, uPhoneNumber, uTokenId);
            accountManager.createRiderAccount(rider, pwd, this);
        } else {
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }
    }


    public void driverSignUp(String uTokenId) {
        String em = txtEmail.getText().toString();
        String pwd = txtPassword.getText().toString();
        String uName = txtName.getText().toString();
        String uPhoneNumber = txtPhoneNumber.getText().toString();


        // Check for empty input
        if (em.isEmpty()) {
            txtEmail.setError("Please enter an email address");
        }
        if (pwd.isEmpty()) {
            txtPassword.setError("Please enter a password ");
        }
        if (uName.isEmpty()) {
            txtName.setError("Please enter your name");
        }
        if (uPhoneNumber.isEmpty()) {
            txtPhoneNumber.setError("Please input your phoneNumber");
        }
        if (!(em.isEmpty() && pwd.isEmpty() && uName.isEmpty() && uPhoneNumber.isEmpty())) {
            Driver driver = new Driver(em, uName, uPhoneNumber, uTokenId, driverCar);
            accountManager.createDriverAccount(driver, pwd, this);
        }
        else {
            Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDonePressed(Car car) {
        driverCar = car;
        driverSignUp(uTokenId);
    }

    @Override
    public void onAccountSignIn(String userType) {

    }

    @Override
    public void onSignInFailure(String e) {

    }

    @Override
    public void onAccountCreated(String accountType) {

        accountManager.uploadProfilePhoto(filePath, this);
        if (accountType.equals(RIDER_TYPE_STRING)) {

            Toast.makeText(this, "You have been registered as a rider", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegistrationActivity.this, RiderMapActivity.class));
            finish();
        }
        else if (accountType.equals(DRIVER_TYPE_STRING)){

            Toast.makeText(this, "You have been registered as a driver", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegistrationActivity.this, DriverMapActivity.class));
            finish();
        }
    }

    @Override
    public void onAccountCreationFailure(String e) {
        Toast.makeText(this, "There was an error creating your account... ", Toast.LENGTH_SHORT).show();
        Log.d(TAG, e);
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
        Toast.makeText(this, "Profile image uploaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageUploadFailure(String e) {
        Toast.makeText(this, "Profile failed to upload", Toast.LENGTH_SHORT).show();
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

