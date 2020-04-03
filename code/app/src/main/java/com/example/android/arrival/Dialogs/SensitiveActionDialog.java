package com.example.android.arrival.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.android.arrival.R;

/**
 * DialogFragment displayed for any action that requires re-authentication of user
 */
public class SensitiveActionDialog extends DialogFragment {


    private OnFragmentInteractionListener listener;
    private String[] authCreds = new String[2];
    private EditText authEmail, authPassword;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_sensitive_action, null);

        authEmail = view.findViewById(R.id.authenticate_email);
        authPassword = view.findViewById(R.id.authenticate_password);


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Input Credentials to perform action")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!(authEmail.length() == 0 || authPassword.length() == 0)) {
                            authCreds[0] = authEmail.getText().toString();
                            authCreds[1] = authPassword.getText().toString();
                            listener.onDonePressed(authCreds);
                        }
                        else {
                            Toast.makeText(getActivity().getBaseContext(), "Please input proper credentials", Toast.LENGTH_SHORT);
                        }
                    }
                }).create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof SensitiveActionDialog.OnFragmentInteractionListener){
            listener = (SensitiveActionDialog.OnFragmentInteractionListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + "must implement OnFragmentInteractionListener");
        }

    }

    public interface OnFragmentInteractionListener{
        void onDonePressed(String[] credentials);
    }
}
