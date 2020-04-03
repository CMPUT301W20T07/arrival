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
import com.example.android.arrival.Dialogs.SensitiveActionDialog;
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
 * Android Activity that displays the Rider's profile screen.
 * Allows Rider to update account information and delete their account.
 */
public class RiderProfileScreenActivity extends AppCompatActivity implements AccountCallbackListener, SensitiveActionDialog.OnFragmentInteractionListener {

    private final static String TAG = "RiderProfileScreenAct";
    private AccountManager accountManager;
    private Rider rider;
    private TextView  name;
    private EditText email, phoneNumber;
    private CircularImageView profilePhoto;
    private Button deleteAccount, updateInfo;
    private final static int GALLERY_RC = 100;
    private final static int UPDATE_RC = 1;
    private final static int DELETE_RC = 0;
    private int dialogChoice;
    private Uri filePath = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountManager = AccountManager.getInstance();
        accountManager.getProfilePhoto(this, accountManager.getUID());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rider_profile_screen);
        rider = (Rider) getIntent().getSerializableExtra("rider");

        email = findViewById(R.id.update_email);
        name = findViewById(R.id.name_TV);
        phoneNumber = findViewById(R.id.update_phone);
        profilePhoto = findViewById(R.id.circularImageViewRider);
        deleteAccount = findViewById(R.id.delete_account_button);
        updateInfo = findViewById(R.id.update_info_btn);

        email.setText(rider.getEmail());
        name.setText(rider.getName());
        phoneNumber.setText(rider.getPhoneNumber());

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChoice = DELETE_RC;
                SensitiveActionDialog sensitiveActionDialog = new SensitiveActionDialog();
                sensitiveActionDialog.show(getSupportFragmentManager(), "Sensitive Action");
            }
        });

        updateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChoice = UPDATE_RC;
                SensitiveActionDialog sensitiveActionDialog = new SensitiveActionDialog();
                sensitiveActionDialog.show(getSupportFragmentManager(), "Sensitive Action");

            }
        });

        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
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
        Toast.makeText(this, "Could not fetch profile data, please try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccountDeleted() {
        startActivity(new Intent(RiderProfileScreenActivity.this, LoginActivity.class));
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
    public void onDonePressed(String[] credentials) {

        boolean matchingPattern = Patterns.EMAIL_ADDRESS.matcher(credentials[0]).matches();
        if (matchingPattern) {
            if (dialogChoice == DELETE_RC) {
                accountManager.deleteAccountData(credentials[0], credentials[1], RiderProfileScreenActivity.this);
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
                rider = new Rider(email.getText().toString(), name.getText().toString(), phoneNumber.getText().toString(), token[0]);
                accountManager.updateRiderAccount(rider, credentials[0], credentials[1], RiderProfileScreenActivity.this);
            }
        }
        else {
            Toast.makeText(this, "Incorrect Email", Toast.LENGTH_SHORT).show();
        }

    }
}
