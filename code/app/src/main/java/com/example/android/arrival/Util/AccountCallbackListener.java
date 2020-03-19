package com.example.android.arrival.Util;

import com.google.firebase.auth.FirebaseUser;

public interface AccountCallbackListener {

    void onAccountSignIn(String userType);

    void onSignInFailure(String e);

    void onAccountCreated(String accountType);

}
