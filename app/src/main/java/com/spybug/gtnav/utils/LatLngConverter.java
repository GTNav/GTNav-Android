package com.spybug.gtnav.utils;

import android.arch.persistence.room.TypeConverter;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class LatLngConverter {

    @TypeConverter
    public static String toString(LatLng value) {
        return String.format("%s,%s", value.getLatitude(), value.getLongitude());
    }

    @TypeConverter
    public static LatLng toLatLng(String values) {
        try {
            String[] split = values.split(",");

            if (split.length == 2) {
                double lat = Double.parseDouble(split[0]);
                double lon = Double.parseDouble(split[1]);
                return new LatLng(lat, lon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; //Error parsing string
    }

}
