package com.example.android.arrival.Util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.android.arrival.Activities.LoginActivity;
import com.example.android.arrival.Activities.MainActivity;
import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements AccountCallbackListener {
    private static final String TAG = "Notifications";
    private AccountManager am;
    private FirebaseAuth fb;
    private FirebaseFirestore db;

    private String newToken;
    private String userID;

    //Need a null constructor for instantiation
    public MyFirebaseMessagingService() {}


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle());
    }

    /**
     * Called whenever a new token is generated
     * @param token : Device token ID
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        newToken = token;


        am = AccountManager.getInstance();
        fb = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = fb.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            am.getAccountType(userID, this);
        }
    }

    public void updateToken(String type) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("tokenId", newToken);

        if (type.equals("rider")) {
            DocumentReference rider = db.collection("riders").document(userID);
            rider.update(updates);
        } else if (type.equals("driver")) {
            DocumentReference driver = db.collection("drivers").document(userID);
            driver.update(updates);
        }
    }



    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody : String of the body of the message
     * @param title : String of the title of the message
     */
    private void sendNotification(String messageBody, String title) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);


        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setSmallIcon(R.id.arrival_logo)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    @Override
    public void onAccountTypeRetrieved(String userType) {
        if (userType != null) {
            updateToken(userType);
        }
    }

    @Override
    public void onAccountTypeRetrieveFailure(String e) {

    }

    @Override
    public void onAccountCreated(String accountType) {

    }

    @Override
    public void onAccountCreationFailure(String e) {

    }

    @Override
    public void onRiderDataRetrieved(Rider rider) {

    }

    @Override
    public void onDriverDataRetrieved(Driver driver) {

    }

    @Override
    public void onDataRetrieveFail(String e) {

    }

    @Override
    public void onAccountDeleted() {

    }

    @Override
    public void onAccountDeleteFailure(String e) {

    }

    @Override
    public void onImageUpload() {

    }

    @Override
    public void onImageUploadFailure(String e) {

    }

    @Override
    public void onPhotoReceived(Uri uri) {

    }

    @Override
    public void onPhotoReceiveFailure(String e) {

    }

    @Override
    public void onAccountUpdated() {

    }

    @Override
    public void onAccountUpdateFailure(String e) {

    }
}
