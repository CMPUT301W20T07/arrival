package com.example.android.arrival.Model;

import android.util.Log;

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

    private List<Request> openRequests;

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
    public void openRequest(Request req) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("requests").document().set(req).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Successfully opened new request in FireStore.");
            }
        });
    }

    public void updateRequest() {

    }

    public void cancelRequest() {

    }

    public void getOpenRequests() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference requestsRef = db.collection(REQUESTS);
        requestsRef.whereEqualTo("status", Request.STATUS_OPEN).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG, "Successfully retrieved from DB");
                openRequests = queryDocumentSnapshots.toObjects(Request.class);
                Log.d(TAG, openRequests.toString());
            }
        });
    }
}
