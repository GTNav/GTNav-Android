package com.spybug.gtnav.models;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class BikeStation {

    public String id, name;
    public LatLng point;
    public int bikesAvailable, bikesDisabled, docksAvailable;
    public Marker marker;

    public BikeStation(String id, String name, double lat, double lon,
                       int bikesAvailable, int bikesDisabled, int docksAvailable) {
        this.id = id;
        this.name = name;
        this.point = new LatLng(lat, lon);
        this.bikesAvailable = bikesAvailable;
        this.bikesDisabled = bikesDisabled;
        this.docksAvailable = docksAvailable;
    }

    @Override
    public String toString() {
        return String.format("Bikes Available: %s\nDocks Available: %s",
                bikesAvailable, docksAvailable);
    }

}
