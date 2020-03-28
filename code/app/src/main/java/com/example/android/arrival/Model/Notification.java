package com.example.android.arrival.Model;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Singleton object used to control all notifications for the app
 */
public class Notification {
    private Notification instance;
    private RequestQueue queue;
    private Context context;
    private String token;
    private String title;
    private String body;
    final private String URL = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AIzaSyB5YNvm0-OupW84u7bv2fqHM3y17pr9vKg";
    final private String contentType = "application/json";

    final String TAG = "NOTIFICATION TAG";



    public Notification(Context ctx, String to, String subject, String message){
        context = ctx;
        token = to;
        title = subject;
        body = message;
        queue = getRequestQueue();
    }

    public Notification getInstance(Context context) {
        if(instance == null) {
            instance = new Notification(context, token, title, body);
        }
        return instance;
    }

    private RequestQueue getRequestQueue() {
        if (queue == null) {
            queue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return queue;
    }



    //TODO cite the youtube tutorial
    public void sendNotification() {
        JSONObject object = new JSONObject();
        try {
            object.put("to", token);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", title);
            notificationObj.put("body", body);
            object.put("notification", notificationObj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, object,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("TAG", "Response: " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("TAG", "Error: " + error.toString());
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



//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        //TOKEN = "c1i0UmO9QL-Xt_ou61AIb_:APA91bEAx4HB07amyw1qMG2a5aak4ojw-ITJnqhYQV8HuWFxkiLYdpqlOT22SGhNfPJSALUU0cQrv0zYhC1UAvPxP6gAhXp8mhY3rqvmFDInl8XE6mCSUX53mTlN01SHGvBsWo8_kw0l";
//        //queue = Volley.newRequestQueue(this);
//        //TOKEN = "eYU7QB_OTyiu5fii2lC1aR:APA91bEZ8YAKFvsR0uUO5u5n-th81uUiblHO-_hojb0Ym7ZQg6-hHlhtxwoBNy-6vzHbqnW7Kx7amyzisITdXzZqxzDpsXYzNxGDYXVk869iKzJBE_Lb9jCcFuk5MuLGJr4e5K4608jk";
//
//        sendNotification();
//    }