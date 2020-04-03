package com.example.android.arrival.Util;

import android.net.Uri;

import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;

/**
 * Interface to be implemented by Activities that want to access
 * account information from Firebase through the AccountManager. Methods
 * determine how to receive the data pulled from the database.
 */
public interface AccountCallbackListener {

    /**
     * called when a user has been successfully signed in
     * @param userType rider or driver
     */
    void onAccountSignIn(String userType);

    /**
     * called when a user has been unsuccessfully signed in
     * @param e error
     */
    void onSignInFailure(String e);

    /**
     * called when a user account has been created
     * @param accountType rider or driver
     */
    void onAccountCreated(String accountType);

    /**
     * called when a user account fails to be created
     * @param e error
     */
    void onAccountCreationFailure(String e);

    /**
     * called when data for rider is retrieved
     * @param rider rider object from firebase
     */
    void onRiderDataRetrieved (Rider rider);

    /**
     * called when data for driver is retrieved
     * @param driver driver object from firebase
     */
    void onDriverDataRetrieved (Driver driver);

    /**
     * called when retrieval of user object is unsuccessful
     * @param e error
     */
    void onDataRetrieveFail (String e);

    /**
     * called when user account is successfully deleted in firebase
     */
    void onAccountDeleted();

    /**
     * called when user account fails to be deleted in firebase
     * @param e error
     */
    void onAccountDeleteFailure(String e);

    /**
     * called when image is successfully uploaded to firebase storage
     */
    void onImageUpload();

    /**
     * called when image fails to upload to firebase storage
     * @param e error
     */
    void onImageUploadFailure(String e);

    /**
     * called when image is successfully received from firebase storage
     * @param uri path to download image
     */
    void onPhotoReceived(Uri uri);

    /**
     * called when image is unable to be fetched from firebase storage
     * @param e error
     */
    void onPhotoReceiveFailure(String e);

    /**
     * called when account details are updated in firebase
     */
    void onAccountUpdated();

    /**
     * called when account details fail to update in firebase
     * @param e
     */
    void onAccountUpdateFailure(String e);
}
