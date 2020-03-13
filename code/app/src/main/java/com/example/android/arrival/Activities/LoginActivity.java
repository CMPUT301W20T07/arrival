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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.UserInfo;

public class LoginActivity extends AppCompatActivity {

    Button signIn;
    TextView signUp;
    EditText email;
    EditText password;
    EditText edit_name;
    private int USER_TYPE_RIDER = 1;
    private int USER_TYPE_DRIVER = 0;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    DocumentReference docRef;
    String name;


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
        name = edit_name.getText().toString();

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


        firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Sign in error occurred", Toast.LENGTH_LONG).show();
                        }
                        else {
//                            docRef = db.collection("users").document(name);
////                            String filePassword = docRef.get(password);
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                String name = user.getDisplayName();
                                String email = user.getEmail();
                                //Uri photoUrl = user.getPhotoUrl();

                                // Check if user's email is verified
                                boolean emailVerified = user.isEmailVerified();

                                // The user's ID, unique to the Firebase project. Do NOT use this value to
                                // authenticate with your backend server, if you have one. Use
                                // FirebaseUser.getIdToken() instead.
                                String uid = user.getUid();
                            }
                        }
                    }
                });
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

    }

    /**
     * check user type
     */
    public void checkUserType(){

    }

}
