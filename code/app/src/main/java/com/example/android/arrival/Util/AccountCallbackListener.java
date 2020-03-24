package com.example.android.arrival.Util;

import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

public interface AccountCallbackListener {

    void onAccountSignIn(String userType);

    void onSignInFailure(String e);

    void onAccountCreated(String accountType);

    void onAccountCreationFailure(String e);

    void onRiderDataRetrieved (Rider rider);
    void onDriverDataRetrieved (Driver driver);
    void onDataRetrieveFail (String e);

    void onAccountDeleted();
    void onAccountDeleteFailure(String e);

    void onImageUpload();
    void onImageUploadFailure(String e);

}
