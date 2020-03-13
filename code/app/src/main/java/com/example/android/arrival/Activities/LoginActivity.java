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
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.arrival.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    Button signIn;
    TextView signUp;
    EditText email;
    EditText password;
    EditText edit_name;
    String userType;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    String TAG = "LoginActivity: ";
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        // Initalize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // View binding
        email = findViewById(R.id.login_email_editText);
        password = findViewById(R.id.login_passWord_editText);
        signUp = findViewById(R.id.sign_up_button);
        signIn = findViewById(R.id.sign_in_button);
        edit_name = findViewById(R.id.user_name_editText);

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
    }


    /**
     * this function handles the backend of signing a user into their account
     */
    public void signUserIn() {

        String emailStr = email.getText().toString();
        String passwordStr = password.getText().toString();

        if (emailStr.isEmpty()) {
            email.setError("Input your email address");
        }
        if (passwordStr.isEmpty()) {
            password.setError("Input your password");
        }
        if (!(emailStr.isEmpty() && passwordStr.isEmpty())) {
            firebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Sign in error occurred", Toast.LENGTH_LONG).show();
                            }
                            else {
                               String uid = firebaseAuth.getCurrentUser().getUid();
                               checkUserType(uid);
                               if (userType != null && userType.equals(DRIVER_TYPE_STRING)) {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                               }
                               else if (userType != null && userType.equals(RIDER_TYPE_STRING)){
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                               }
                               else {
                                   Log.d(TAG, "onComplete: " + uid + userType);
                                   Toast.makeText(LoginActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
                               }
                            }
                        }
                    });
        }
        else {
                Toast.makeText(LoginActivity.this, "Input relevant data", Toast.LENGTH_SHORT).show();
            }
    }

    /**
     * checks user type
     * @param uid takes in userID to search the users document on firestore
     * @return userType
     */
    public void checkUserType(String uid){


        firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .whereEqualTo(uid, true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                Map<String, Object> documentSnapshotData = documentSnapshot.getData();
                                userType = (String) documentSnapshotData.get("type");
                                Log.d(TAG, "Check user type: " + userType);
                            }

                        }
                        else {
                            Log.d(TAG, "onComplete: there was an error getting " + uid + " user type");
                            Toast.makeText(LoginActivity.this, "There was an error getting user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}
