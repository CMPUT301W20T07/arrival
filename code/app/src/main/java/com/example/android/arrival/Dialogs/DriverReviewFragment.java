package com.example.android.arrival.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.android.arrival.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriverReviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverReviewFragment extends DialogFragment {

    private static final String TAG = "ReviewActivity";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference reviewsRef;

    private TextView txtLikes;
    private TextView txtDislikes;

    public DriverReviewFragment() {
        // Required empty public constructor
    }

    public static DriverReviewFragment newInstance() {
        DriverReviewFragment fragment = new DriverReviewFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        reviewsRef = db.collection("ratings");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_driver_review, null);
        txtLikes = view.findViewById(R.id.txtLikes);
        txtDislikes = view.findViewById(R.id.txtDislikes);

        getReviews();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Ratings")
                .setNegativeButton("Done", null)
                .create();
    }

    private void getReviews() {
        reviewsRef.whereEqualTo("driverID", auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> ds = queryDocumentSnapshots.getDocuments();

                        int up = 0;
                        int down = 0;
                        for(DocumentSnapshot s : ds) {
                            Log.d(TAG, ds.toString());
                            Log.d(TAG, s.get("rating").toString());

                            long r = (long) s.get("rating");
                            if(r == 1) {
                                up ++;
                            } else {
                                down ++;
                            }
                        }
                        txtLikes.setText(("" + up));
                        txtDislikes.setText(("" + down));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "getting reviews failed");
            }
        });
    }
}
