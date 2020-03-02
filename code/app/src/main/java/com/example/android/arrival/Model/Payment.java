package com.example.android.arrival.Model;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.Date;

public class Payment {

    private Rider rider;
    private Driver driver;
    private Date date;
    private Barcode QRCode;
    private float fare;

    public float getFare() {
        return fare;
    }

    public void setFare(float fare) {
        this.fare = fare;
    }

    public Rider getRider() {
        return rider;
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Barcode getQRCode() {
        return QRCode;
    }

    public void setQRCode(Barcode QRCode) {
        this.QRCode = QRCode;
    }
}


