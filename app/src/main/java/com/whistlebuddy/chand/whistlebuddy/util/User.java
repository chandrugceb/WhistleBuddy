package com.whistlebuddy.chand.whistlebuddy.util;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by chand on 22-01-2018.
 */

@IgnoreExtraProperties
public class User {

    public String displayname;
    public String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String displayname, String email) {
        this.displayname = displayname;
        this.email = email;
    }
}