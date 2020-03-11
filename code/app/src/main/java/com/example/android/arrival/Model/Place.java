package com.example.android.arrival.Model;

import java.io.Serializable;

public class Place implements Comparable<Place>, Serializable {
    private String name;
    private String address;
    private Double lat;
    private Double lon;


    public Place(String name, String address, Double lat, Double lon){
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    public Place() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    //Returns 0 if they are the same and 1 if they are different
    @Override
    public int compareTo(Place place){
        if (this.address.compareToIgnoreCase(place.address) == 0 || this.name.compareToIgnoreCase(place.name) == 0
        || this.lat.equals(place.lat) || this.lon.equals(place.lon)) {
            return 0;
        }
        else{
            return 1;
        }
    }
}
