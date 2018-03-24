package com.spybug.gtnav.models;


import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class Bus {

    public int id, heading;
    public LatLng point;
    public String dirTag;
    public Marker marker;

    public Bus(int id, double lat, double lon, int heading, String dirTag) {
        this.id = id;
        this.point = new LatLng(lat, lon);
        this.heading = heading;
        this.dirTag = dirTag;
    }

    @Override
    public String toString() {
        return String.format("Bus ID: %s", id);
    }

}
