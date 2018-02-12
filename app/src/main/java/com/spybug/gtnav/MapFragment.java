package com.spybug.gtnav;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.utils.MapFragmentUtils;


public class MapFragment extends SupportMapFragment {

    public MapView map;

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
    }

}
