package com.example.android.arrival.Model;

import java.io.Serializable;

/**
 * Data Object superclass that represents all Users
 */
public class User implements Serializable {

    private String id;
    private String email;
    private String name;
    private String phoneNumber;
    private String tokenId;

    public User() {
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public User(String email, String name, String phoneNumber, String tokenId) {
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.tokenId = tokenId;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return this.id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

}
