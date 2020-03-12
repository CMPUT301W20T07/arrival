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

public class LoginActivity extends AppCompatActivity {

    Button signIn;
    TextView signUp;
    EditText email;
    EditText password;
    private int USER_TYPE_RIDER = 1;
    private int USER_TYPE_DRIVER = 0;
    FirebaseAuth firebaseAuth;


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
                            Toast.makeText(LoginActivity.this, "Sign in error occured", Toast.LENGTH_LONG).show();
                        }
                        else {

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
