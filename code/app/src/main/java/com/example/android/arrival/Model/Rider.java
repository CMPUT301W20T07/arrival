package com.example.android.arrival.Model;

import java.io.Serializable;

/**
 * Data Object class that represents Riders
 */
public class Rider extends User implements Serializable {

    public Rider() {
        // Must have a constructor with no params to be pulled as Object from FireStore.
    }

    public Rider(String email, String name, String phoneNumber, String tokenId) {
        super(email, name, phoneNumber, tokenId);
    }

}
