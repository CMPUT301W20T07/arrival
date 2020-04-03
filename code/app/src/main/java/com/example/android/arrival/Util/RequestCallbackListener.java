package com.example.android.arrival.Util;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Interface to be implemented by Activities that want to access
 * request information from Firebase through the RequestManager. Methods
 * determine how to receive the data pulled from the database.
 */
public interface RequestCallbackListener {

    /**
     * Defines what should happen when the Firebase RequestManager
     * starts to retrieve data from the database. For example,
     * show a loading icon.
     */
    void onCallbackStart();

    /**
     * Defines that the view should update.
     */
    void update();

    /**
     * Define what should happen when a Request is successfully retrieved
     * using the RequestManager.getOpenRequests() method from the db.
     * @param snapshot
     */
    void onGetRequestSuccess(DocumentSnapshot snapshot);

    /**
     * Define what should happen when data is successfully retrieved
     * using the RequestManger.getOpenRequests() methods from the
     * database and how it should update.
     * @param snapshot
     */
    void onGetOpenSuccess(QuerySnapshot snapshot);

    /**
     * Define what should happen when data is successfully retrieved
     * using the RequestManger.getRiderRequests() methods from the
     * database and how it should update.
     * @param snapshot
     */
    void onGetRiderRequestsSuccess(QuerySnapshot snapshot);

    /**
     * Define what should happen when data is successfully retrieved
     * using the RequestManger.getDriverRequests() methods from the
     * database and how it should update.
     * @param snapshot
     */
    void onGetDriverRequestsSuccess(QuerySnapshot snapshot);
}
