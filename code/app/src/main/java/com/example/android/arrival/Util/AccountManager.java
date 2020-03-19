package com.example.android.arrival.Util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.android.arrival.Activities.LoginActivity;
import com.example.android.arrival.Activities.RegistrationActivity;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Singleton object used to manage all actions pertaining to an account
 */
public class AccountManager {

    public static final String TAG = "AccountManager: ";
    private static AccountManager instance;
    private static final String RIDER_TYPE_STRING = "rider";
    private static final String DRIVER_TYPE_STRING = "driver";
    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore db;
    private CollectionReference userRef;
    private CollectionReference riderRef;
    private CollectionReference driverRef;


    private AccountManager() {
        db = FirebaseFirestore.getInstance();
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
     * this method creates an account for a driver, registering all necessary data in firebase
     * @param driver
     * @param password
     * @param context
     */
    public void createDriverAccount(Driver driver, String password, Context context) {


        firebaseAuth.createUserWithEmailAndPassword(driver.getEmail(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = firebaseAuth.getCurrentUser().getUid();
                    DocumentReference usersDocReference = userRef.document(userId);

                    // add user type to look up table "users"
                    Map<String, String> user = new HashMap<>();
                    user.put("type", DRIVER_TYPE_STRING);
                    usersDocReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context,"You have been added as driver", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "There was a problem creating your account", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "User onFailure: " + e);

                        }
                    });

                    // add user data to driver table
                    DocumentReference driverDocumentReference = driverRef.document(userId);
                    driverDocumentReference.set(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Your data has been stored", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "There was a problem creating your account", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Driver onFailure: " + e);

                        }
                    });
                }
                else {
                    Toast.makeText(context, "There was a problem creating your account", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: create driver:" + task.getException().toString());
                }
            }
        });
    }

    public void createRiderAccount(Rider rider, String password, Context context) {
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
                            Toast.makeText(context, "You have been added as rider", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "There was a problem creating your account", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "User onFailure: " + e);

                        }
                    });

                    // add user data to driver table
                    DocumentReference riderDocumentReference = riderRef.document(userId);
                    riderDocumentReference.set(rider).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Your data has been stored", Toast.LENGTH_SHORT).show();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "There was a problem creating your account", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Rider onFailure: " + e);

                                }
                            });
                }
                else {
                    Toast.makeText(context, "There was a problem creating your account", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: create rider:" + task.getException().toString());
                }
            }
        });
    }

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
/*
    public void deleteCurrentUser (Context context) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String uid = firebaseUser.getUid();



        if (accType.equals(DRIVER_TYPE_STRING)) {
            deleteDriverData(context, uid);
            deleteAccount(firebaseUser);
        }
        else {
            deleteRiderData(context, uid);
            deleteAccount(firebaseUser);
        }

    }
*/
    public void signInWithGoogle() {

    }

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
            }
        });

    }


}
