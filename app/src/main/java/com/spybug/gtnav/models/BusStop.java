package com.spybug.gtnav.models;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class BusStop {

    public String id, name;
    public LatLng point;
    public Marker marker;

    public BusStop(String id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.point = new LatLng(lat, lon);
    }

    @Override
    public String toString() {
        return String.format("Estimation time would go here");
    }

}
