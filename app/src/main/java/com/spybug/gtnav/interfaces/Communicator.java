package com.spybug.gtnav.interfaces;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.spybug.gtnav.models.BikeStation;
import com.spybug.gtnav.models.Bus;
import com.spybug.gtnav.models.BusStop;

import java.util.List;

public interface Communicator {
    public void passRouteToMap(LatLng[] points);

    public void passBusRouteToMap(List<List<LatLng>> points, String routeColor);

    public void passBusLocationsToMap(List<Bus> buses, String routeColor);

    public void passBikeStationsToMap(List<BikeStation> bikeStations);

    public void passBusStopsToMap(List<BusStop> busStops, String routeColor);

    public void clearBuses();
}
