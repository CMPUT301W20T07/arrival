package com.example.android.arrival.Util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
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
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    private FirebaseStorage storage;




    private AccountManager() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = db.collection("users");
        riderRef = db.collection("riders");
        driverRef = db.collection("drivers");
        storage = FirebaseStorage.getInstance();


    }

    public static AccountManager getInstance() {
        if(instance == null) {
            instance = new AccountManager();
        }
        return instance;
    }


    /**
     * this function returns user data to the callback listener
     * @param listener callback listener to give values to
     */
    public void getUserData(final AccountCallbackListener listener) {

        String uid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference docRef = userRef.document(uid);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // find user type
                String type = documentSnapshot.get("type").toString();
                Log.d(TAG, "onSuccess: " + type);


                if (type.equals(RIDER_TYPE_STRING)) {
                    DocumentReference documentReference = riderRef.document(uid);
                    documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Rider riderData = documentSnapshot.toObject(Rider.class);
                            listener.onRiderDataRetrieved(riderData);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "get data onFailure: " + e.toString());
                            listener.onDataRetrieveFail(e.toString());
                        }
                    });
                }
                else if (type.equals(DRIVER_TYPE_STRING)) {
                    DocumentReference documentReference = driverRef.document(uid);
                    documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Driver driverData = documentSnapshot.toObject(Driver.class);
                            listener.onDriverDataRetrieved(driverData);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "get data onFailure: " + e.toString());
                            listener.onDataRetrieveFail(e.toString());

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.toString());
                listener.onDataRetrieveFail(e.toString());
            }
        });



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
                            Log.d(TAG, "User onFailure: " + e.toString());
                            listener.onAccountCreationFailure(e.toString());
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
                            listener.onAccountCreationFailure(e.toString());
                            Log.d(TAG, "Driver onFailure: " + e);

                        }
                    });
                }
                else {
                    Log.d(TAG, "onComplete: create driver:" + task.getException().toString());
                    listener.onAccountCreationFailure(task.getException().toString());
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
                            listener.onAccountCreationFailure(e.toString());
                        }
                    });

                    // add user data to driver table
                    DocumentReference riderDocumentReference = riderRef.document(userId);
                    riderDocumentReference.set(rider).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listener.onAccountCreated(RIDER_TYPE_STRING);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Rider onFailure: " + e);
                            listener.onAccountCreationFailure(e.toString());
                        }
                    });
                }
                else {
                    Log.d(TAG, "onComplete: create rider:" + task.getException().toString());
                    listener.onAccountCreationFailure(task.getException().toString());
                }
            }
        });
    }

    /**
     * given a user id this function finds the user type related to that user and returns it to the callback listener
     * @param uid user id
     * @param listener callback listener that the function gives final output to
     */
    public String getAccountType(String uid, final AccountCallbackListener listener) {


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

        return accountType[0];
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

    /**
     * this method deletes current user's account data from everywhere in firebase
     * @param listener callback listener
     */
    public void deleteAccountData(final AccountCallbackListener listener) {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        String uid = user.getUid();

        userRef.document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // get user type
                String type = documentSnapshot.get("type").toString();


                userRef.document(uid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {

                    // success listener for deleting user from users look up table

                    @Override
                    public void onSuccess(Void aVoid) {


                        if (type.equals(DRIVER_TYPE_STRING)) {
                            driverRef.document(uid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {

                                // success listener for deleting driver from drivers table

                                @Override
                                public void onSuccess(Void aVoid) {
                                    firebaseAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {

                                        // success listener for deleting driver in firebase auth

                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            listener.onAccountDeleted();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: firebaseAuthAccountDelete: " + e.toString());
                                            listener.onAccountDeleteFailure(e.toString());
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: accountDataDeleted: " + e.toString());
                                    listener.onAccountDeleteFailure(e.toString());
                                }
                            });
                        }
                        else if (type.equals(RIDER_TYPE_STRING)) {
                            riderRef.document(uid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {

                                // success listener for deleting rider data from riders table

                                @Override
                                public void onSuccess(Void aVoid) {
                                    firebaseAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {

                                        // on success listener for deleting user from firebase auth

                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            listener.onAccountDeleted();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: firebaseAuthAccountDelete: " + e.toString());
                                            listener.onAccountDeleteFailure(e.toString());
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: accountDataDeleted: " + e.toString());
                                    listener.onAccountDeleteFailure(e.toString());
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: delete from users Table:" + e.toString());
                        listener.onAccountDeleteFailure(e.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: delete data" + e.toString());
                listener.onAccountDeleteFailure(e.toString());
            }
        });

    }

    public void uploadProfilePhoto(@NonNull Uri filePath, AccountCallbackListener listener) {

        String uid = firebaseAuth.getCurrentUser().getUid();
        StorageReference ref = storage.getReference().child("images/" + uid);
        ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: " + ref.getPath());
                listener.onImageUpload();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.toString());
                listener.onImageUploadFailure(e.toString());
            }
        });
    }

    public void getProfilePhoto(final AccountCallbackListener listener) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        StorageReference reference = storage.getReference().child("images/" + uid);
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                listener.onPhotoReceived(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: getProfilePhoto" + e.toString());
                listener.onPhotoReceiveFailure(e.toString());
            }
        });
    }


}
