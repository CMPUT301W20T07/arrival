package com.example.android.arrival.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.arrival.Dialogs.ForgotPasswordDialog;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.example.android.arrival.Util.AccountCallbackListener;
import com.example.android.arrival.Util.AccountManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements AccountCallbackListener {

    private int RC_SIGN_IN = 500;
    Button signIn;
    TextView signUp;
    TextView forgot_password;
    EditText email;
    EditText password;
    String TAG = "LoginActivity: ";
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";
    AccountManager accountManager;
    SignInButton signInGoogle;
    GoogleSignInClient googleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        accountManager = AccountManager.getInstance();

        // View binding
        email = findViewById(R.id.login_email_editText);
        password = findViewById(R.id.login_passWord_editText);
        signUp = findViewById(R.id.sign_up_button);
        signIn = findViewById(R.id.sign_in_button);
        signInGoogle = findViewById(R.id.sign_in_google_button);
        forgot_password = findViewById(R.id.forgot_password);

        /* Code that handles sign in with google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("772065063254-c6bmasbskc5e4o386g8tvgao851tdn7k.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        signInGoogle.setSize(SignInButton.SIZE_ICON_ONLY);
        signInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });*/
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUserIn();
            }
        });

        signUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForgotPasswordDialog();
            }
        });


    }

    public void signUserIn () {
        String emailStr = email.getText().toString();
        String passwordStr = password.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()){
            email.setError("Please enter a valid email address");
        }
        if (emailStr.isEmpty()) {
            email.setError("Input your email address");
        }
        if (passwordStr.isEmpty()) {
            password.setError("Input your password");
        }
        if (!(emailStr.isEmpty() && passwordStr.isEmpty())) {
            accountManager.signInUser(emailStr, passwordStr, this);
        }
        else {
            Log.d(TAG, "signUserIn: fail");
            Animation shake = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.shake);
            email.startAnimation(shake);
            password.startAnimation(shake);
            Toast.makeText(LoginActivity.this, "Input relevant data", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onAccountSignIn(String accountType) {
        Log.d("TOKEN", "calling to check for token");
        checkForToken(accountType);
        Log.d(TAG, "onAccountSignIn: " + accountType);
        if (accountType.equals(DRIVER_TYPE_STRING)) {
            Toast.makeText(this, "Signing in as driver...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, DriverMapActivity.class);
            startActivity(intent);
            finish();
        }
        else if (accountType.equals(RIDER_TYPE_STRING)){
            Toast.makeText(this, "Signing in as rider...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, RiderMapActivity.class);
            startActivity(intent);
            finish();
        }
        else {

        }
    }

    @Override
    public void onSignInFailure(String e) {
        Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
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


    public void openForgotPasswordDialog() {
        ForgotPasswordDialog.display(getSupportFragmentManager());
    }

    public void checkForToken(String type) {
        Log.d("TOKEN", "In check for token");
        FirebaseAuth fb = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String[] token = new String[1];

        FirebaseUser user = fb.getCurrentUser();
        String uid = user.getUid();

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

                        Log.d("TOKEN", "Token: " + token[0]);
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("tokenId", token[0]);

                        if (type.equals("rider")) {
                            DocumentReference rider = db.collection("riders").document(uid);
                            rider.set(updates, SetOptions.merge());
                            Log.d("TOKEN", "should have updated rider token");
                        }
                        else if (type.equals("driver")) {
                            DocumentReference driver = db.collection("drivers").document(uid);
                            driver.update(updates);
                        }
                    }
                });

    }












/* Code that handles sign in with google stuff
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));

    }*/
}

