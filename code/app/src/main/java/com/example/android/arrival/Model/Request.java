package com.example.android.arrival.Model;

public class Request {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_DENIED = "DENIED";

    private String id;
    private String status;
    private float fare;
    private GeoLocation startLocation;
    private GeoLocation endLocation;
    private Rider rider;
    private Driver driver;

    public Request(){
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public Request(Rider rider, GeoLocation start, GeoLocation end, float fare) {
        this.status = this.STATUS_OPEN;
        this.rider = rider;
        this.driver = null;
        this.startLocation = start;
        this.endLocation = end;
        this.fare = fare;
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

    public GeoLocation getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(GeoLocation startLocation) {
        this.startLocation = startLocation;
    }

    public GeoLocation getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(GeoLocation endLocation) {
        this.endLocation = endLocation;
    }

    public Rider getRider() {
        return rider;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
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
