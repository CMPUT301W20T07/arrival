package com.example.android.arrival.Model;

public class Driver extends User {

    public Driver() {
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public Driver(String username, String password) {
        super(username, password);
    }
}
