package com.example.android.arrival.Model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RequestManager {

    // I think ideally, this class would be a singleton or static class

    public RequestManager() {

    }

    /**
     * Opens a Request in the FireStore Cloud Database.
     * @param req a Request object to be opened in FireStore
     */
    public void openRequest(Request req) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("requests").document().set(req);
    }

    public void updateRequest() {

    }

    public void cancelRequest() {

    }


}
