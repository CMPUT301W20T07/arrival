package com.example.android.arrival.Dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.android.arrival.Activities.LoginActivity;
import com.example.android.arrival.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordDialog extends DialogFragment {

    public static final String TAG = "password_fragment";

    private Button button;
    private EditText resetEmail;
    private Button resetButton;
    private FirebaseAuth resetAuth;

    public static ForgotPasswordDialog display(FragmentManager fragmentManager){

        ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog();

        forgotPasswordDialog.show(fragmentManager, TAG);

        return forgotPasswordDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);*/
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.forgot_password_dialog, container, false);

        button = view.findViewById(R.id.clear_button);

        //Set OnClickListener for Reset Button
        resetAuth = FirebaseAuth.getInstance();
        resetButton = view.findViewById(R.id.reset_submit);
        resetEmail = view.findViewById(R.id.password_reset_edittext);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                String userEmail = resetEmail.getText().toString();

                if(TextUtils.isEmpty(userEmail)){
                    resetEmail.setError("Please enter a valid email address.");
                }

                if(!isEmailValid(userEmail)){
                    resetEmail.startAnimation(shake);
                    Toast.makeText(getContext(),"Email is invalid.",Toast.LENGTH_SHORT).show();
                }

                else{
                    resetAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getContext(),"Please check your email address for a reset link.",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                            else{
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(getContext(),"A(n) " + errorMessage + "error occurred.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog newDialog = getDialog();
        if(newDialog != null){
            int i = ViewGroup.LayoutParams.MATCH_PARENT;
            int j = ViewGroup.LayoutParams.MATCH_PARENT;
            newDialog.getWindow().setLayout(i, j);
            newDialog.getWindow().setWindowAnimations(R.style.AppTheme_EnterSlide);
        }

    }

    boolean isEmailValid(CharSequence email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
