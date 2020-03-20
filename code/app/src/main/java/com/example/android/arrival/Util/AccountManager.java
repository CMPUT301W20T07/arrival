package com.example.android.arrival.Util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


/**
 * Singleton object used to manage all actions pertaining to an account
 */
public class AccountManager {

    public static final String TAG = "AccountManager: ";
    private static AccountManager instance;
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";
    private FirebaseAuth firebaseAuth;
    private GoogleSignInOptions gso;
    private CollectionReference userRef;
    private CollectionReference riderRef;
    private CollectionReference driverRef;



    private AccountManager() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = db.collection("users");
        riderRef = db.collection("riders");
        driverRef = db.collection("drivers");


    }

    public static AccountManager getInstance() {
        if(instance == null) {
            instance = new AccountManager();
        }
        return instance;
    }

    /**
     * this function takes in a driver object and stores it in firebase
     * @param driver driver object
     * @param password password to create account with
     * @param listener callback listener
     */
    public void createDriverAccount(Driver driver, String password, final AccountCallbackListener listener) {


        firebaseAuth.createUserWithEmailAndPassword(driver.getEmail(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = task.getResult().getUser().getUid();
                    DocumentReference usersDocReference = userRef.document(userId);

                    // add user type to look up table "users"
                    Map<String, String> user = new HashMap<>();
                    user.put("type", DRIVER_TYPE_STRING);
                    usersDocReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: " + DRIVER_TYPE_STRING);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "User onFailure: " + e);

                        }
                    });

                    // add user data to driver table
                    DocumentReference driverDocumentReference = driverRef.document(userId);
                    driverDocumentReference.set(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listener.onAccountCreated(DRIVER_TYPE_STRING);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Log.d(TAG, "Driver onFailure: " + e);

                        }
                    });
                }
                else {
                    Log.d(TAG, "onComplete: create driver:" + task.getException().toString());
                }
            }
        });
    }

    /**
     * this function takes in a rider object and stores it in firebase
     * @param rider rider object
     * @param password password to create account with
     * @param listener callback listener
     */
    public void createRiderAccount(Rider rider, String password, final AccountCallbackListener listener) {

        firebaseAuth.createUserWithEmailAndPassword(rider.getEmail(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = firebaseAuth.getCurrentUser().getUid();
                    DocumentReference usersDocReference = userRef.document(userId);

                    // add user type to look up table "users"
                    Map<String, String> user = new HashMap<>();
                    user.put("type", RIDER_TYPE_STRING);
                    usersDocReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: " + RIDER_TYPE_STRING);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "User onFailure: " + e);
                        }
                    });

                    // add user data to driver table
                    DocumentReference riderDocumentReference = riderRef.document(userId);
                    riderDocumentReference.set(rider).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listener.onAccountCreated(RIDER_TYPE_STRING);
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Rider onFailure: " + e);

                                }
                            });
                }
                else {
                    Log.d(TAG, "onComplete: create rider:" + task.getException().toString());
                }
            }
        });
    }

    /**
     * given a user id this function finds the user type related to that user and returns it to the callback listener
     * @param uid user id
     * @param listener callback listener that the function gives final output to
     */
    public void getAccountType(String uid, final AccountCallbackListener listener) {


        final String[] accountType = new String[1];
        DocumentReference documentReference = userRef.document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String docData = documentSnapshot.get("type").toString();
                accountType[0] = docData;
                listener.onAccountSignIn(accountType[0]);
                Log.d(TAG, "onSuccess: " + docData);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: getAccountType: " + e.toString());
                accountType[0] = null;
            }
        });


    }

    public void updateDriverInfo() {

    }


    public void updateRiderInfo() {

    }

    private void deleteDriverData(Context context, String uid) {

        userRef.document(uid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: deleteDriverData: deleted " + uid + " from users table");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: deleteDriverData: failed to delete " + uid + " from users: " + e.toString());
                Toast.makeText(context, "Account not deleted", Toast.LENGTH_SHORT).show();
            }
        });

        driverRef.document(uid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: deleteDriverData: deleted " + uid + " from drivers table");
                Toast.makeText(context, "Data deleted successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: deleteDriverData: failed to delete " + uid + " from drivers: " + e.toString());
                Toast.makeText(context, "Account not deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRiderData(Context context, String uid) {

    }

    private void deleteAccount(FirebaseUser user) {

        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: user account deleted from firebaseFireAuth");
                }
                else {
                    Log.d(TAG, "onComplete: Failed to delete from firebaseFireAuth with error: " + task.getException().toString());
                }
            }
        });
    }

    public void deleteCurrentUser (Context context, final AccountCallbackListener listener) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String uid = firebaseUser.getUid();
        String accType = "hello";

        if (accType.equals(DRIVER_TYPE_STRING)) {
            deleteDriverData(context, uid);
            deleteAccount(firebaseUser);
        }
        else {
            deleteRiderData(context, uid);
            deleteAccount(firebaseUser);
        }

    }

    public void signInWithGoogle() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


    }

    /**
     * This function signs the user in
     * @param email user email
     * @param password user password
     * @param listener callback listener to be used by activity that calls it
     */
    public void signInUser(String email, String password, final AccountCallbackListener listener) {


        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.d(TAG, "onSuccess: " + authResult.getUser().getUid());
                getAccountType(authResult.getUser().getUid(), listener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.toString());
                listener.onSignInFailure(e.toString());

            }
        });

    }


}
