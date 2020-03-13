package com.example.android.arrival.Model;

public class User {

    private String username;
    private String name;
    private String phoneNumber;


    public User() {
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public User(String username, String name, String phoneNumber) {
        this.username = username;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
