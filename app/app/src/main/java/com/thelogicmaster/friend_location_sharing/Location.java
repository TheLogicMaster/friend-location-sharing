package com.thelogicmaster.friend_location_sharing;

import com.google.android.gms.maps.model.LatLng;

public class Location {

    public final double longitude, latitude;
    public final long time;
    public LatLng latLng;

    public Location(double longitude, double latitude, long time) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = time;
        latLng = new LatLng(latitude, longitude);
    }
}
