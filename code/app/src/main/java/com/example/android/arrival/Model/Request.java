package com.example.android.arrival.Model;

import java.io.Serializable;
import java.util.UUID;

public class Request implements Serializable {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_DENIED = "DENIED";

    private String id;
    private String status;
    private float fare;
    private Place startLocation;
    private Place endLocation;
    private String rider;
    private String driver;

    public Request(){
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public Request(String rider, Place start, Place end, float fare) {
        this.id = generateID();
        this.status = this.STATUS_OPEN;
        this.rider = rider;
        this.driver = null;
        this.startLocation = start;
        this.endLocation = end;
        this.fare = fare;
    }

    // For testing
    public Request(String id, String rider, Place start, Place end, float fare) {
        this.id = id;
        this.status = this.STATUS_OPEN;
        this.rider = rider;
        this.driver = null;
        this.startLocation = start;
        this.endLocation = end;
        this.fare = fare;
    }


    public String getID() {
        return id;
    }

    /**
     * Return a random, unique id.
     * @return
     */
    public String generateID() {
        return "" + System.nanoTime();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getFare() {
        return fare;
    }

    public void setFare(float fare) {
        this.fare = fare;
    }

    public Place getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Place startLocation) {
        this.startLocation = startLocation;
    }

    public Place getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Place endLocation) {
        this.endLocation = endLocation;
    }

    public String getRider() {
        return rider;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String toString() {
        return "{STATUS: " + getStatus() +
                ", RIDER: " +  getRider() +
                ", DRIVER: " + getDriver() +
                ", START: " + getStartLocation().toString() +
                ", END: "  + getEndLocation().toString() +
                ", FARE: " + getFare() + "}";
    }

}
