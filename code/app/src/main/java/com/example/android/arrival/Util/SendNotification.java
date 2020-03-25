package com.example.android.arrival.Util;

import android.app.Notification;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.*;


import androidx.appcompat.app.AppCompatActivity;

import com.example.android.arrival.Activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendNotification extends AppCompatActivity {
    final private String URL = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AIzaSyB5YNvm0-OupW84u7bv2fqHM3y17pr9vKg";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    private RequestQueue queue;

    String TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TOKEN = "c1i0UmO9QL-Xt_ou61AIb_:APA91bEAx4HB07amyw1qMG2a5aak4ojw-ITJnqhYQV8HuWFxkiLYdpqlOT22SGhNfPJSALUU0cQrv0zYhC1UAvPxP6gAhXp8mhY3rqvmFDInl8XE6mCSUX53mTlN01SHGvBsWo8_kw0l";
        queue = Volley.newRequestQueue(this);
        //TOKEN = "eYU7QB_OTyiu5fii2lC1aR:APA91bEZ8YAKFvsR0uUO5u5n-th81uUiblHO-_hojb0Ym7ZQg6-hHlhtxwoBNy-6vzHbqnW7Kx7amyzisITdXzZqxzDpsXYzNxGDYXVk869iKzJBE_Lb9jCcFuk5MuLGJr4e5K4608jk";

        sendNotification();
    }

    private void sendNotification() {
        JSONObject object = new JSONObject();
        try {
            object.put("to", TOKEN);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "TestTokenSend");
            notificationObj.put("body", "Please fucking work");
            object.put("notification", notificationObj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, object,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Codes here will run on success
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Codes here will run on error
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", contentType);
                    header.put("authorization", serverKey);
                    return header;
                }
            };

            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
