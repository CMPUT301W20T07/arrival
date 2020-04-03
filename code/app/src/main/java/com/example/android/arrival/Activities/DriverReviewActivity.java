package com.example.android.arrival.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.arrival.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class DriverReviewActivity extends AppCompatActivity {

    private static final String TAG = "ReviewActivity";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference reviewsRef;

    private TextView txtLikes;
    private TextView txtDislikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_review);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        reviewsRef = db.collection("ratings");

        txtLikes = findViewById(R.id.txtLikes);
        txtDislikes = findViewById(R.id.txtDislikes);

        getReviews();
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
