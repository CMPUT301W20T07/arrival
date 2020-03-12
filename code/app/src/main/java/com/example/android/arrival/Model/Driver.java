package com.example.android.arrival.Model;

public class Driver extends User {


    private Car car;
    public Driver() {
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public Driver(String username, String name, String phoneNumber, Car car) {
        super(username, name, phoneNumber);
        this.car = car;
    }
}
