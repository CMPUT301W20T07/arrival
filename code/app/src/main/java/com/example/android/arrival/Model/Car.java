package com.example.android.arrival.Model;

import java.io.Serializable;

public class Car implements Serializable {

    private String make;
    private String model;
    private String Year;
    private String Plate;
    private String color;

    public Car(String make, String model, String year, String plate, String color) {
        this.make = make;
        this.model = model;
        Year = year;
        Plate = plate;
        this.color = color;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return Year;
    }

    public void setYear(String year) {
        Year = year;
    }

    public String getPlate() {
        return Plate;
    }

    public void setPlate(String plate) {
        Plate = plate;
    }
}
