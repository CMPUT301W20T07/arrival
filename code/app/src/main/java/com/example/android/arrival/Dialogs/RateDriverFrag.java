package com.example.android.arrival.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RateDriverFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RateDriverFrag extends DialogFragment {

    private FirebaseFirestore fb;

    private Driver driver;
    private String riderID;
    private String driverID;

    private TextView txtDriverName;
    private Button btnUpvote;
    private Button btnDownvote;

    public RateDriverFrag() {
        // Required empty public constructor
    }

    public static RateDriverFrag newInstance(Driver driver, String driverID) {
        RateDriverFrag fragment = new RateDriverFrag();
        Bundle args = new Bundle();
        args.putSerializable("driver", driver);
        args.putSerializable("driverID", driverID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();

        if (args != null) {
            driver = (Driver) args.getSerializable("driver");
            driverID = (String) args.getSerializable("driverID");
            riderID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_rate_driver, null);

        fb = FirebaseFirestore.getInstance();

        txtDriverName = view.findViewById(R.id.txtDriverName);
        btnUpvote = view.findViewById(R.id.btnUpvote);
        btnDownvote = view.findViewById(R.id.btnDownvote);

        txtDriverName.setText("How would was your ride with " + driver.getName() + "?");

        btnUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRating(1);
                dismiss();
            }
        });

        btnDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRating(-1);
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Rate Driver")
                .setNegativeButton("Cancel", null)
                .create();
    }

    public void addRating(int rate) {
        Map<String, Object> data = new HashMap<>();
        data.put("driverID", driverID);
        data.put("riderID", riderID);
        data.put("rating", rate);
        fb.collection("ratings").document().set(data);
    }
}
