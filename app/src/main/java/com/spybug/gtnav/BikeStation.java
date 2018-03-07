package com.spybug.gtnav;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class BikeStation {

    public String id, name;
    public LatLng point;
    public int bikesAvailable, bikesDisabled, docksAvailable;

    public BikeStation(String id, String name, LatLng point,
                       int bikesAvailable, int bikesDisabled, int docksAvailable) {
        this.id = id;
        this.name = name;
        this.point = point;
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
