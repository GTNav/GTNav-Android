package com.spybug.gtnav;


import com.mapbox.mapboxsdk.geometry.LatLng;

public interface Communicator {
    public void passRouteToMap(LatLng[] points);
}
