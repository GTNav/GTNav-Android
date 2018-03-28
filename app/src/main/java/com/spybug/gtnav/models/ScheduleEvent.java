package com.spybug.gtnav.models;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class ScheduleEvent {
    private String name;
    private String[] recurringDays;
    private String time;
    private LatLng location;

    ScheduleEvent(String name, String time, LatLng location, String[] recurringDays) {
        this.name = name;
        this.time = time;
        this.location = location;
        this.recurringDays = recurringDays;
    }

    public String getName() {
        return name;
    }

    public String[] getRecurringDays() {
        return recurringDays;
    }

    public String getTime() {
        return time;
    }

    public LatLng getLocation() {
        return location;
    }
}
