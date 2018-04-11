package com.spybug.gtnav.models;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BusStop {

    public String id, name, routeName;
    public LatLng point;
    public Marker marker;
    public List<Integer> estimation_times;

    public BusStop(String id, String name, String routeName, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.point = new LatLng(lat, lon);
        this.routeName = routeName;
        estimation_times = new ArrayList<>();
    }

    @Override
    public String toString() {
        if (!estimation_times.isEmpty()) {
            String snippetText = "";
            for (int i = 0; i < estimation_times.size(); i++) {
                snippetText += String.format("Next bus in %s mins", estimation_times.get(i));
                if (i < estimation_times.size() - 1) {
                    snippetText += "\n";
                }
            }
            return snippetText;
        }
        else {
            return "No estimation times available";
        }
    }

}
