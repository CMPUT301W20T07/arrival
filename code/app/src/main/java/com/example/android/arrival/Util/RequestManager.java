package com.example.android.arrival.Util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.android.arrival.Model.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

/**
 * Singleton object used to push, pull, update, and delete
 * Requests in the Arrival Firebase FireStore Cloud Database.
 */
public class RequestManager {

    public static final String REQUESTS = "requests";
    public static final String TAG = "RequestManager";

    private static RequestManager instance;
    private FirebaseFirestore db;
    private CollectionReference requestRef;

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
        db = FirebaseFirestore.getInstance();
        requestRef = db.collection(REQUESTS);
        requestRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                // If error
                if(e != null) {
                    return;
                }
            }
        });
    }

    /**
     * Opens a request in the FireStore Cloud Database.
     * @param req a Request object to be opened in FireStore.
     */
    public void openRequest(Request req, final RequestCallbackListener listener) {
        requestRef.document(req.getID()).set(req).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG + "-open", "Successfully opened new request " + req.getID() + " in FireStore.");
                getRequest(req.getID(), listener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to create new request for " + req.getID());
            }
        });
    }

    /**
     * Deletes a request from the FireStore CLoud Database. Used for testing.
     * @param id
     */
    public void deleteRequest(String id, final RequestCallbackListener listener) {
        requestRef.document(id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG + "-delete", "Successfully deleted request " + id + " from FireStore.");
                        listener.update();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG + "-delete", "Failed to delete deleted request " + id + " from FireStore: " + e.toString());
                    }
                });
    }

    /**
     * Updates a request in the FireStore Cloud Database. For example, can be used
     * to change the status of a request, or to assign a driver.
     * @param req
     */
    public void updateRequest(Request req, final RequestCallbackListener listener) {
        requestRef.document(req.getID())
                .set(req)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG + "-update", "Successfully updated request " + req.getID() + "in FireStore.");
                        getRequest(req.getID(), listener);
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
     * Retrieves the request with the given ID from the FireStore Cloud Database.
     * @param id
     * @param listener
     */
    public void getRequest(String id, final RequestCallbackListener listener) {
        requestRef.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                listener.onGetRequestSuccess(documentSnapshot);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to get request with id" + id);
            }
        });
    }

    /**
     * Retrieves all currently open requests from the FireStore Cloud Database.
     * @param listener
     */
    public void getOpenRequests(final RequestCallbackListener listener) {
        requestRef.whereEqualTo("status", Request.OPEN).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG + "-getOpen", "Successfully retrieved open requests from DB. ");

                listener.onGetOpenSuccess(queryDocumentSnapshots);
            }
        });
    }

    /**
     * Retrieves all the requests made by the given rider from the FireStore Cloud Database.
     * @param rider
     * @param listener
     */
    public void getRiderRequests(String rider, final RequestCallbackListener listener) {
        requestRef.whereEqualTo("rider", rider).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG + "-getRider", "Successfully retrieved rider " + rider + "+'s requests from DB. ");
                List<Request> userRequests = queryDocumentSnapshots.toObjects(Request.class);
                if(userRequests.size() > 0) {
                    Log.d(TAG + "-getRider", userRequests.toString());

                    listener.onGetRiderRequestsSuccess(queryDocumentSnapshots);
                } else {
                    Log.d(TAG + "-getRider", "No Requests matched the username provided. ");
                }
            }
        });
    }

    /**
     * Retrieves all the active requests made by the given rider from the FireStore Cloud Database.
     */
    public void getRiderOpenRequests(String rider, final RequestCallbackListener listener) {
        Log.d(TAG, "Getting open rider requests....");
        requestRef.whereEqualTo("rider", rider).whereLessThanOrEqualTo("status", Request.AWAITING_PAYMENT).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG + "-getRider", "Successfully retrieved rider " + rider + "'s OPEN requests from DB. ");
                List<Request> userRequests = queryDocumentSnapshots.toObjects(Request.class);
                if(userRequests.size() > 0) {
                    Log.d(TAG + "-getRider", userRequests.toString());

                    listener.onGetRiderOpenRequestsSuccess(queryDocumentSnapshots);
                } else {
                    Log.d(TAG + "-getRider", "The user provided has no open requests!");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    /**
     * Retrieves all the requests accepted by the given driver from the FireStore Cloud Database.
     */
    public void getDriverRequests(String driver, final RequestCallbackListener listener) {
        requestRef.whereEqualTo("driver", driver).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG + "-getDriv", "Successfully retrieved driver " + driver + "'s requests from DB. ");
                List<Request> userRequests = queryDocumentSnapshots.toObjects(Request.class);
                if(userRequests.size() > 0) {
                    Log.d(TAG + "-getDriv", userRequests.toString());

                    listener.onGetDriverRequestsSuccess(queryDocumentSnapshots);
                } else {
                    Log.d(TAG + "-getDriv", "No Requests matched the username provided. ");
                }
            }
        });
    }

}
