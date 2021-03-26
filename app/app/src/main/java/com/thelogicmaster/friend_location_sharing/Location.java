package com.thelogicmaster.friend_location_sharing;

public class Location {

    public final double longitude, latitude;
    public final long time;

    public Location(double longitude, double latitude, long time) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = time;
    }
}
