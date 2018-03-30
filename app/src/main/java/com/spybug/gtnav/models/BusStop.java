package com.spybug.gtnav.models;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class BusStop {

    public String id, name;
    public LatLng point;
    public Marker marker;
    public Integer estimation_time;

    public BusStop(String id, String name, double lat, double lon, int estimation_time) {
        this.id = id;
        this.name = name;
        this.point = new LatLng(lat, lon);
        this.estimation_time = estimation_time;
    }

    @Override
    public String toString() {
        if (estimation_time != -1) {
            return String.format("Next bus in %s minutes", estimation_time);
        }
        else {
            return "No estimation times available";
        }
    }

}
