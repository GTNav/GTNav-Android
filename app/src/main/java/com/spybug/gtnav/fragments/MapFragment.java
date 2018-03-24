package com.spybug.gtnav.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.utils.MapFragmentUtils;
import com.spybug.gtnav.models.BikeStation;
import com.spybug.gtnav.models.Bus;
import com.spybug.gtnav.utils.HelperUtil;
import com.spybug.gtnav.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.spybug.gtnav.utils.HelperUtil.drawableToBitmap;
import static com.spybug.gtnav.utils.HelperUtil.rotateBitmap;


public class MapFragment extends SupportMapFragment {

    private final List<OnMapReadyCallback> mapReadyCallbackList = new ArrayList<>();
    public MapView map;
    private MapboxMap mapboxMap;
    private IconFactory iconFactory;
    private Icon start_icon, destination_icon, bikestation_icon;
    private Bitmap bus_icon;
    private String lastRouteColor;

    private List<Polyline> busRoutes;
    private HashMap<Integer, Bus> busesHM;
    private HashMap<String, BikeStation> bikeStationsHM;

    /**
     * Creates the fragment view hierarchy.
     *
     * @param inflater           Inflater used to inflate content.
     * @param container          The parent layout for the map fragment.
     * @param savedInstanceState The saved instance state for the map fragment.
     * @return The view created
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        map = (MapView) super.onCreateView(inflater, container, savedInstanceState);

        Resources resources = getResources();
        iconFactory = IconFactory.getInstance((map).getContext());
        Drawable startMarkerDrawable = resources.getDrawable(R.drawable.start_marker);

        Bitmap start_marker_icon = drawableToBitmap(startMarkerDrawable);
        lastRouteColor = "";

        start_icon = iconFactory.fromBitmap(start_marker_icon);
        destination_icon = iconFactory.defaultMarker();
        //bus_icon = iconFactory.defaultMarker();
        bikestation_icon = iconFactory.defaultMarker();

        busRoutes = new ArrayList<>();
        busesHM = new HashMap<>();
        bikeStationsHM = new HashMap<>();

        return map;
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public static MapFragment newInstance(@Nullable MapboxMapOptions mapboxMapOptions) {
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(MapFragmentUtils.createFragmentArgs(mapboxMapOptions));
        return mapFragment;
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        for (OnMapReadyCallback onMapReadyCallback : mapReadyCallbackList) {
            onMapReadyCallback.onMapReady(mapboxMap);
        }
    }

    public void drawDirectionsRoute(LatLng[] points) {
        if (mapboxMap != null) {
            mapboxMap.clear();
            //Draw Points on Map
            mapboxMap.addPolyline(new PolylineOptions()
                    .add(points)
                    .alpha(0.8f)
                    .color(Color.parseColor("#20BEFB"))
                    .width(4));

            LatLng firstPoint = points[0];
            LatLng lastPoint = points[points.length - 1];

            LatLngBounds latLngBounds = new LatLngBounds.Builder()
                    .include(firstPoint)
                    .include(lastPoint)
                    .build();

            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 175), 2000);

            mapboxMap.addMarker(new MarkerOptions()
                    .position(firstPoint)
                    .title("Start")
                    .icon(start_icon));

            mapboxMap.addMarker(new MarkerOptions()
                    .position(lastPoint)
                    .title("Destination")
                    .icon(destination_icon));
        }
    }

    public void drawBusesRoute(List<List<LatLng>> points, String routeColor) {
        if (busRoutes.size() > 0) {
            for (Polyline route : busRoutes) {
                route.remove();
            }
        }

        LatLngBounds.Builder routeBounds = new LatLngBounds.Builder();

        for (List<LatLng> pointList : points) {
            busRoutes.add(mapboxMap.addPolyline(new PolylineOptions()
                    .addAll(pointList)
                    .color(Color.parseColor(routeColor))
                    .alpha(0.6f)
                    .width(4)));

            routeBounds.includes(pointList);
        }

        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(routeBounds.build(), 100));
    }

    public void drawBusLocations(List<Bus> buses, String routeColor) {
        //TODO: Change icon color based on route
        //TODO: Use Symbol Layer for markers so rotation works properly
        // https://www.mapbox.com/android-docs/api/map-sdk/5.2.1/com/mapbox/mapboxsdk/style/layers/SymbolLayer.html

        for (Bus bus : buses) {
            Bus storedBus = busesHM.get(bus.id);
            //If bus ID already exists, then update the marker in the Bus object
            if (storedBus != null) {
                Marker storedMarker = storedBus.marker;
                ValueAnimator markerAnimator = ObjectAnimator.ofObject(storedMarker, "position", new HelperUtil.LatLngEvaluator(),
                        storedMarker.getPosition(), bus.point);
                markerAnimator.setDuration(1500);
                markerAnimator.start();
                Icon rotatedIcon = iconFactory.fromBitmap(rotateBitmap(getBusIcon(routeColor), bus.heading));
                storedMarker.setIcon(rotatedIcon);
            }
            //Create a new marker for the bus and add it to the HashMap
            else {
                bus.marker = mapboxMap.addMarker(new MarkerOptions()
                        .position(bus.point)
                        .title(Integer.toString(bus.id))
                        .icon(iconFactory.fromBitmap(rotateBitmap(getBusIcon(routeColor), bus.heading))));
                busesHM.put(bus.id, bus);
            }
        }
    }

    public void drawBikeStations(List<BikeStation> bikeStations) {
        for (BikeStation bikeStation : bikeStations) {
            BikeStation storedBikeStation = bikeStationsHM.get(bikeStation.id);
            //If bikeStation ID already exists, then update the marker
            if (storedBikeStation != null) {
                Marker storedMarker = storedBikeStation.marker;
                storedMarker.setTitle(bikeStation.name);
                storedMarker.setSnippet(bikeStation.toString());
            }
            //Create a new marker for the bike station and add it to the HashMap
            else {
                bikeStation.marker = mapboxMap.addMarker(new MarkerOptions()
                        .position(bikeStation.point)
                        .title(bikeStation.name)
                        .snippet(bikeStation.toString())
                        .icon(bikestation_icon));
                bikeStationsHM.put(bikeStation.id, bikeStation);
            }
        }
    }

    private Bitmap getBusIcon(String routeColor) {
        if (!routeColor.equals(lastRouteColor)) {
            Drawable icon_drawable = getResources().getDrawable(R.drawable.bus_icon_white).mutate();
            icon_drawable.setColorFilter(new PorterDuffColorFilter(Color.parseColor(routeColor), PorterDuff.Mode.SRC_IN));
            bus_icon = Bitmap.createScaledBitmap(drawableToBitmap(icon_drawable), 50, 85, false);
            lastRouteColor = routeColor;
        }
        return bus_icon;
    }

    public void clearMap() {
        if (mapboxMap != null) {
            clearBuses();
            mapboxMap.clear();
        }
    }

    public void clearBuses() {
        if (mapboxMap != null) {
            for (Bus b : busesHM.values()) {
                mapboxMap.removeAnnotation(b.marker);
            }
            busesHM.clear();
        }
    }

    /**
     * Called when the fragment is visible for the users.
     */
    @Override
    public void onStart() {
        super.onStart();
        map.onStart();
    }

    /**
     * Called when the fragment is ready to be interacted with.
     */
    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    /**
     * Called when the fragment is pausing.
     */
    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    /**
     * Called when the fragment state needs to be saved.
     *
     * @param outState The saved state
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }

    /**
     * Called when the fragment is no longer visible for the user.
     */
    @Override
    public void onStop() {
        super.onStop();
        map.onStop();
    }

    /**
     * Called when the fragment receives onLowMemory call from the hosting Activity.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    /**
     * Called when the fragment is view hiearchy is being destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        map.onDestroy();
        mapReadyCallbackList.clear();
    }

    /**
     * Sets a callback object which will be triggered when the MapboxMap instance is ready to be used.
     *
     * @param onMapReadyCallback The callback to be invoked.
     */
    public void getMapAsync(@NonNull final OnMapReadyCallback onMapReadyCallback) {
        if (mapboxMap == null) {
            mapReadyCallbackList.add(onMapReadyCallback);
        } else {
            onMapReadyCallback.onMapReady(mapboxMap);
        }
    }

}
