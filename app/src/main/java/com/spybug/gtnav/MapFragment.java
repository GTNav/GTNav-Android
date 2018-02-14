package com.spybug.gtnav;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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

import java.util.ArrayList;
import java.util.List;

import static com.spybug.gtnav.HelperUtil.drawableToBitmap;


public class MapFragment extends SupportMapFragment {

    private final List<OnMapReadyCallback> mapReadyCallbackList = new ArrayList<>();
    public MapView map;
    private MapboxMap mapboxMap;
    private Icon start_icon, destination_icon;
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
        IconFactory iconFactory = IconFactory.getInstance((map).getContext());
        Drawable startMarkerDrawable = resources.getDrawable(R.drawable.start_marker);

        Bitmap start_marker_icon = drawableToBitmap(startMarkerDrawable);
        start_icon = iconFactory.fromBitmap(start_marker_icon);
        destination_icon = iconFactory.defaultMarker();

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

    public void clearMap() {
        if (mapboxMap != null) {
            mapboxMap.clear();
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
