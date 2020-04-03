package com.example.android.arrival.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.android.arrival.Dialogs.CarDetailsDialog;
import com.example.android.arrival.Dialogs.SensitiveActionDialog;
import com.example.android.arrival.Model.Car;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.example.android.arrival.Util.AccountCallbackListener;
import com.example.android.arrival.Util.AccountManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.IOException;

/**
 * Android Activity that displays the Driver's profile screen.
 * Allows Driver to update account information and delete their account.
 */
public class DriverProfileScreenActivity extends AppCompatActivity implements AccountCallbackListener, CarDetailsDialog.OnFragmentInteractionListener, SensitiveActionDialog.OnFragmentInteractionListener {

    private final static String TAG = "DriverProfileScreenAct";
    private Driver driver;
    private Car car;
    private TextView name;
    private EditText emailET, phoneET;
    private Button updateInfoButton, deleteAccountButton, editCarButton;
    private AccountManager accountManager;
    private CircularImageView profilePhoto;
    private Uri filePath = null;
    private final static int GALLERY_RC = 100;
    private final static int UPDATE_RC = 1;
    private final static int DELETE_RC = 0;
    private int dialogChoice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        driver = (Driver) getIntent().getSerializableExtra("driver");
        car = driver.getCar();
        accountManager = AccountManager.getInstance();
        accountManager.getProfilePhoto(this, accountManager.getUID());

        name = findViewById(R.id.nameTVDriver);
        emailET = findViewById(R.id.update_email_driver);
        phoneET = findViewById(R.id.update_phone_driver);
        updateInfoButton = findViewById(R.id.update_info_btn_driver);
        deleteAccountButton = findViewById(R.id.delete_account_button_driver);
        editCarButton = findViewById(R.id.update_car_info);
        profilePhoto = findViewById(R.id.circularImageViewDriver);

        name.setText(driver.getName());
        emailET.setText(driver.getEmail());
        phoneET.setText(driver.getPhoneNumber());

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChoice = DELETE_RC;
                SensitiveActionDialog sensitiveActionDialog = new SensitiveActionDialog();
                sensitiveActionDialog.show(getSupportFragmentManager(), "Sensitive Action");

            }
        });

        updateInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChoice = UPDATE_RC;
                SensitiveActionDialog sensitiveActionDialog = new SensitiveActionDialog();
                sensitiveActionDialog.show(getSupportFragmentManager(), "Sensitive Action");

            }
        });

        editCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CarDetailsDialog carDetailsDialog = new CarDetailsDialog();
                carDetailsDialog.show(getSupportFragmentManager(), "car");
            }
        });
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });

    }

    // demonuts article by EDITORIAL TEAM (https://demonuts.com/author/editorial-team/)
    // "Pick Image From Gallery Or Camera In Android Studio Programmatically"
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
                profilePhoto.setImageBitmap(bitmap);
                accountManager.uploadProfilePhoto(filePath, this);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "onActivityResult: " + e.toString());
            }

            Log.d(TAG, "onActivityResult: " + filePath.toString());

        }
    }


    @Override
    public void onAccountTypeRetrieved(String userType) {

    }

    @Override
    public void onAccountTypeRetrieveFailure(String e) {

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
        startActivity(new Intent(DriverProfileScreenActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public void onAccountDeleteFailure(String e) {
        Toast.makeText(this, e, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onImageUpload() {
        Toast.makeText(this, "Photo updated!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageUploadFailure(String e) {
        Toast.makeText(this, "Could not update photo", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPhotoReceived(Uri uri) {
        Glide.with(this).load(uri).into(profilePhoto);
        Log.d(TAG, "onPhotoReceived: " + uri.toString());

    }

    @Override
    public void onPhotoReceiveFailure(String e) {
        Toast.makeText(this, "Could not fetch photo", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAccountUpdated() {
        Toast.makeText(this, "Info successfully updated!", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    @Override
    public void onAccountUpdateFailure(String e) {
        Toast.makeText(this, "There was an error updating your account", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDonePressed(Car s) {
        car = s;
    }

    @Override
    public void onDonePressed(String[] credentials) {

        boolean matchingPattern = Patterns.EMAIL_ADDRESS.matcher(credentials[0]).matches();

        if (matchingPattern) {
            if (dialogChoice == DELETE_RC) {
                accountManager.deleteAccountData(credentials[0], credentials[1], DriverProfileScreenActivity.this);
            } else if (dialogChoice == UPDATE_RC) {
                String[] token = new String[1];
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                    return;
                                }
                                // Get new Instance ID token
                                token[0] = task.getResult().getToken();
                                Log.d(TAG, token[0]);
                            }
                        });
                driver = new Driver(emailET.getText().toString(), name.getText().toString(), phoneET.getText().toString(), token[0], car);
                accountManager.updateDriverAccount(driver, credentials[0], credentials[1], DriverProfileScreenActivity.this);
            }
        }
        else {
            Toast.makeText(this, "Incorrect Email", Toast.LENGTH_SHORT).show();
        }

    }
}
