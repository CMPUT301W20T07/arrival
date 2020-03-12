package com.example.android.arrival.Model;

public class Rider extends User {

    public Rider() {
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public Rider(String username, String name, String phoneNumber) {
        super(username, name, phoneNumber);
    }

}
