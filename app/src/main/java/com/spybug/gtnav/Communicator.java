package com.spybug.gtnav;

import com.mapbox.mapboxsdk.geometry.LatLng;
import java.util.List;

public interface Communicator {
    public void passRouteToMap(LatLng[] points);

    public void passBusRouteToMap(List<LatLng> points, String routeColor);

    public void passBusLocationsToMap(List<LatLng> buses, String routeColor);
}
