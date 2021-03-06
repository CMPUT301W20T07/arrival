package com.example.android.arrival.Model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Object class that represents Requests
 */
public class Request implements Serializable {

    // Request status identifiers
    public static final int OPEN = 0;
    public static final int ACCEPTED = 1;
    public static final int PICKED_UP = 2;
    public static final int AWAITING_PAYMENT = 3;
    public static final int COMPLETED = 4;
    public static final int CANCELLED = 5;

    // Dictionary of status identifier text
    public static final Map<Integer, String> STATUS;
    static {
        Map<Integer, String> tmap = new HashMap<>();
        tmap.put(OPEN, "OPEN");
        tmap.put(ACCEPTED, "ACCEPTED");
        tmap.put(PICKED_UP, "PICKED UP");
        tmap.put(AWAITING_PAYMENT, "AWAITING PAYMENT");
        tmap.put(COMPLETED, "COMPLETED");
        tmap.put(CANCELLED, "CANCELLED");
        STATUS = Collections.unmodifiableMap(tmap);
    }

    private String id;
    private int status;
    private float fare;
    private Place startLocation;
    private Place endLocation;
    private String rider;
    private String driver;

    public Request() {
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public Request(String rider, Place start, Place end, float fare) {
        this.id = generateID();
        this.status = this.OPEN;
        this.rider = rider;
        this.driver = null;
        this.startLocation = start;
        this.endLocation = end;
        this.fare = fare;
    }

    // For testing
    public Request(String id, String rider, Place start, Place end, float fare) {
        this.id = id;
        this.status = this.OPEN;
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
     * Return a random, unique ID based off the system's nano-time.
     * Should be updated in the future to generate a more reliable
     * unique ID.
     * @return
     */
    public String generateID() {
        return "" + System.nanoTime();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
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
        return "{ID: " + getID() +
                ", STATUS: " + getStatus() +
                ", RIDER: " + getRider() +
                ", DRIVER: " + getDriver() +
                ", START: " + getStartLocation().toString() +
                ", END: " + getEndLocation().toString() +
                ", FARE: " + getFare() + "}";
    }

    public Boolean equals(Request r) {
        if (id.equals(r.getID()) &&
                status == r.getStatus() &&
                fare == r.getFare() &&
                rider.equals(r.getRider()) &&
                (driver == null || driver.equals(r.getDriver())) &&
                startLocation.equals(r.getStartLocation()) &&
                endLocation.equals(r.getEndLocation())) {
            return true;
        }

        return false;
    }

}
