package com.example.android.arrival.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.arrival.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    EditText Name;
    EditText phoneNumber;
    EditText email;
    EditText password;
    EditText confirmPassword;
    Button driverSignUp;
    Button riderSignUp;
    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registration);

        email = findViewById(R.id.register_email_editText);
        password = findViewById(R.id.register_password_editText);

        driverSignUp = findViewById(R.id.sign_up_driver);
        riderSignUp = findViewById(R.id.sign_up_rider);
        firebaseAuth = FirebaseAuth.getInstance();

        driverSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        riderSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String em = email.getText().toString();
                String pwd = password.getText().toString();

                // checking if something wasnt input
                if (em.isEmpty()) {
                    email.setError("Please enter an email address");
                }
                else if (pwd.isEmpty()) {
                    password.setError("Please enter a password ");
                }
                else if (!(em.isEmpty() && pwd.isEmpty())) {
                    firebaseAuth.createUserWithEmailAndPassword(em, pwd).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegistrationActivity.this, "Sign up Unsuccessful", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(RegistrationActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                }

            }
        });




    }
}
