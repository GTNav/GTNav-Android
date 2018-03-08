package com.spybug.gtnav;

import com.mapbox.mapboxsdk.geometry.LatLng;
import java.util.List;

public interface Communicator {
    public void passRouteToMap(LatLng[] points);

    public void passBusRouteToMap(List<List<LatLng>> points, String routeColor);

    public void passBusLocationsToMap(List<Bus> buses, String routeColor);

    public void passBikeStationsToMap(List<BikeStation> bikeStations);

    public void clearBuses();
}
