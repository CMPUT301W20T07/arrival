package com.example.android.arrival.Model;

import java.io.Serializable;

/**
 * Data Object class that represents Drivers
 */
public class Driver extends User implements Serializable {

    private Car car;

    public Driver() {
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public Driver(String email, String name, String phoneNumber, String tokenId, Car car) {
        super(email, name, phoneNumber, tokenId);
        this.car = car;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
