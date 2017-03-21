package com.example.android.quakereport;

/**
 * Created by dzenang on 27.2.2017.
 *
 * Class which represents one earthquake with all its data
 */

public class Earthquake {

    // Members
    private double mMagnitude;
    private String mLocation;
    private long mMilliseconds;
    private String mUrl;

    // Constructors
    public Earthquake(double magnitude, String location, long date, String url) {
        mMagnitude = magnitude;
        mLocation = location;
        mMilliseconds = date;
        mUrl = url;
    }

    // Getters
    public double getMagnitude() {
        return mMagnitude;
    }

    public String getLocation() {
        return mLocation;
    }

    public long getMilliseconds() {
        return mMilliseconds;
    }

    public String getUrl() {
        return mUrl;
    }
}