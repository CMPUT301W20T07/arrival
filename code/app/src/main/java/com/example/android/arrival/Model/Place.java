package com.example.android.arrival.Model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Place implements Comparable<Place>, Serializable {

    private String name;
    private String address;
    private double lat;
    private double lon;

//    private LatLng latLng;

    public Place() {

    }

    public Place(String name, String address, double lat, double lon){
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
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

    public void setLatLng(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public void setLatLng(LatLng latLng) {
        this.lat = latLng.latitude;
        this.lon = latLng.longitude;
    }

    @Exclude
    public LatLng getLatLng() {
        return new LatLng(lat, lon);
    }

    public double getLat() {
        return this.lat;
    }

    public double getLon() {
        return this.lon;
    }

//    public double getLat() {
//        return lat;
//    }
//
//    public void setLat(double lat) {
//        this.lat = lat;
//    }
//
//    public double getLon() {
//        return lon;
//    }
//
//    public void setLon(double lon) {
//        this.lon = lon;
//    }

//    //Returns 0 if they are the same and 1 if they are different
//    @Override
//    public int compareTo(Place place){
////        if (this.address.compareToIgnoreCase(place.address) == 0 || this.name.compareToIgnoreCase(place.name) == 0
////        || this.lat.equals(place.lat) || this.lon.equals(place.lon)) {
////            return 0;
////        }
//        if (this.address.compareToIgnoreCase(place.address) == 0 || this.name.compareToIgnoreCase(place.name) == 0
//                || this.lat == place.lat || this.lon == place.lon) {
//            return 0;
//        }
//        else{
//            return 1;
//        }
//    }

    public boolean equals(Place place) {
        if (this.address.compareToIgnoreCase(place.address) == 0 || this.name.compareToIgnoreCase(place.name) == 0
                || (this.getLat() == place.getLat() && this.getLon() == place.getLon())) {
            return true;
        }
        return false;
    }

    //Returns 0 if they are the same and 1 if they are different
    @Override
    public int compareTo(Place place){
//        if (this.address.compareToIgnoreCase(place.address) == 0 || this.name.compareToIgnoreCase(place.name) == 0
//        || this.lat.equals(place.lat) || this.lon.equals(place.lon)) {
//            return 0;
//        }
        if (this.address.compareToIgnoreCase(place.address) == 0 || this.name.compareToIgnoreCase(place.name) == 0
                || (this.getLat() == place.getLat() && this.getLon() == place.getLon())) {
            return 0;
        }
        else{
            return 1;
        }
    }

    public String toString() {
        return name;
    }
}
