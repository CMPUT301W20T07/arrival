package com.example.android.arrival.Model;

public class Driver extends User {

    private Car car;

    public Driver() {
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public Driver(String email, String name, String phoneNumber, Car car) {
        super(email, name, phoneNumber);
        this.car = car;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
