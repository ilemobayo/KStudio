package com.musicplayer.aow.ui.auth.google.model;

import com.musicplayer.aow.utils.user.UserDetails;

import java.util.HashMap;

/**
 * Created by Gino Osahon on 04/03/2017.
 */

public class User {

    private String fullName;
    private String phone;
    private String email;
    private HashMap<String,Object> timestampJoined;
    private UserDetails.UserPhoneDetails other;

    public User() {
    }

    /**
     * Use this constructor to create new User.
     * Takes user name, email and timestampJoined as params
     *
     * @param timestampJoined
     */
    public User(String mFullName, String mPhoneNo, String mEmail, HashMap<String, Object> timestampJoined, UserDetails.UserPhoneDetails others) {
        this.fullName = mFullName;
        this.phone = mPhoneNo;
        this.email = mEmail;
        this.timestampJoined = timestampJoined;
        this.other = other;
    }


    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public HashMap<String, Object> getTimestampJoined() {
        return timestampJoined;
    }
}
