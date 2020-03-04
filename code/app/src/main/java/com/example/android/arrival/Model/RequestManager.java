package com.example.android.arrival.Model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RequestManager {

    public static final String REQUESTS = "requests";
    public static final String TAG = "RequestManager";

    public static RequestManager instance;

    private List<Request> openRequests; // Available to Drivers

    /**
     * Returns the RequestManager object associated with the current Android application.
     * Methods are invoked with respect to the current RequestManager object.
     * @return the <code>RequestManager</code> object associated with the
     * current Android application.
     */
    public static RequestManager getInstance() {
        if(instance == null) {
            instance = new RequestManager();
        }
        return instance;
    }

    private RequestManager() {
        openRequests = new ArrayList<>();
    }

    /**
     * Opens a Request in the FireStore Cloud Database.
     * @param req a Request object to be opened in FireStore.
     */
    public void openNewRequest(Request req) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("requests").document(req.getID()).set(req).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG + "-open", "Successfully opened new request " + req.getID() + "in FireStore.");
                getOpenRequests(); // Update current list of open requests
            }
        });
    }

    /**
     * Update a Request in the Cloud FireStore Database. Can be used
     * to change the status of a request.
     * @param req
     */
    public void updateRequest(Request req) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference requestRef = db.collection(REQUESTS).document(req.getID());

        db.collection(REQUESTS).document(req.getID())
                .set(req)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG + "-update", "Successfully updated request " + req.getID() + "in FireStore.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed");
                    }
                });
    }

    /**
     * Update the list of open requests from the FireStore Cloud Database.
     */
    public void getOpenRequests() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference requestsRef = db.collection(REQUESTS);
        requestsRef.whereEqualTo("status", Request.STATUS_OPEN).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG + "-getOpen", "Successfully retrieved open requests from DB. ");
                openRequests = queryDocumentSnapshots.toObjects(Request.class);
                Log.d(TAG + "-getOpen", openRequests.toString());
            }
        });
    }

    /**
     *
     */
    public void getRiderRequests(String rider) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference requestsRef = db.collection(REQUESTS);
        requestsRef.whereEqualTo("rider", rider).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG + "-getRider", "Successfully retrieved rider " + rider + "+'s requests from DB. ");
                List<Request> userRequests = queryDocumentSnapshots.toObjects(Request.class);
                if(userRequests.size() > 0) {
                    Log.d(TAG + "-getRider", userRequests.toString());
                } else {
                    Log.d(TAG + "-getRider", "No Requests matched the username provided. ");
                }
            }
        });
    }

    /**
     *
     */
    public void getDriverRequests(String driver) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference requestsRef = db.collection(REQUESTS);
        requestsRef.whereEqualTo("driver", driver).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG + "-getDriv", "Successfully retrieved driver " + driver + "'s requests from DB. ");
                List<Request> userRequests = queryDocumentSnapshots.toObjects(Request.class);
                if(userRequests.size() > 0) {
                    Log.d(TAG + "-getDriv", userRequests.toString());
                } else {
                    Log.d(TAG + "-getDriv", "No Requests matched the username provided. ");
                }
            }
        });
    }

}
