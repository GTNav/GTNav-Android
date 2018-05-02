package com.spybug.gtnav.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.spybug.gtnav.R;
import com.spybug.gtnav.fragments.BikesOverlayFragment;
import com.spybug.gtnav.fragments.BottomNavbarFragment;
import com.spybug.gtnav.fragments.BusMapOverlayFragment;
import com.spybug.gtnav.fragments.DirectionsMenuFragment;
import com.spybug.gtnav.fragments.FaqFragment;
import com.spybug.gtnav.fragments.FeedbackFragment;
import com.spybug.gtnav.fragments.MainMapOverlayFragment;
import com.spybug.gtnav.fragments.MapFragment;
import com.spybug.gtnav.fragments.ScheduleFragment;
import com.spybug.gtnav.fragments.SettingsFragment;
import com.spybug.gtnav.models.BikeStation;
import com.spybug.gtnav.models.Bus;
import com.spybug.gtnav.models.BusStop;

import java.util.List;

import static com.spybug.gtnav.utils.HelperUtil.convertDpToPixel;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationEngineListener, PermissionsListener {

    private final static String ROOT_TAG = "root_fragment";

    private MapboxMap map;
    private MapFragment mapFragment;
    private NavigationView navMenu;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private BottomNavbarFragment bottomBarFragment;
    private SharedPreferences prefs;

    private enum State {
        MAIN,
        DIRECTIONS,
        BUSES,
        BIKES,
        SCHEDULE,
        SETTINGS,
        FAQ,
        FEEDBACK
    }

    private State currentState;

    private static final LatLngBounds GT_BOUNDS = new LatLngBounds.Builder()
            .include(new LatLng(33.753312, -84.421579))
            .include(new LatLng(33.797474, -84.372656))
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentState = null;
        PreferenceManager.setDefaultValues(this, R.xml.settings, false); //set default prefs if not set before
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        navMenu = findViewById(R.id.nav_view);
        navMenu.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();

            LatLng GT = new LatLng(33.7756, -84.3963);

            MapboxMapOptions options = new MapboxMapOptions();
            options.styleUrl(Style.MAPBOX_STREETS);
            options.camera(new CameraPosition.Builder()
                    .target(GT)
                    .zoom(14)
                    .build());

            mapFragment = MapFragment.newInstance(options);
            bottomBarFragment = new BottomNavbarFragment();

            transaction.add(R.id.map_frame, mapFragment, "com.mapbox.map");
            transaction.add(R.id.bottom_bar_frame, bottomBarFragment, "com.gtnav.bottomBar");
            transaction.commit();
            fragmentManager.executePendingTransactions();

            openMainMapFragment();
        } else {
            mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                map.setMinZoomPreference(11);
                UiSettings uiSettings = map.getUiSettings();
                uiSettings.setTiltGesturesEnabled(false);
                uiSettings.setCompassEnabled(true);

                Context context = getBaseContext();
                uiSettings.setCompassMargins(0, (int) convertDpToPixel(75, context), (int) convertDpToPixel(10, context), 0);

                map.setLatLngBoundsForCameraTarget(GT_BOUNDS);

                enableLocationPlugin();
            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (currentState == State.MAIN) {
                supportFinishAfterTransition(); //Exit app if on main page
            }
            else {
                openMainMapFragment(); //Go back to main map fragment if not already there
            }
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            openMainMapFragment();
        } else if (id == R.id.nav_directions) {
            openDirectionsFragment();
        } else if (id == R.id.nav_buses) {
            openBusesFragment();
        } else if (id == R.id.nav_bikes) {
            openBikesFragment();
        } else if (id == R.id.nav_schedule) {
            openScheduleFragment();
        } else if (id == R.id.nav_settings) {
            openSettingsFragment();
        } else if (id == R.id.nav_faq) {
            openFaqFragment();
        } else if (id == R.id.nav_feedback) {
            openFeedbackFragment();
        }

        closeDrawer();
        item.setChecked(true);
        return true;
    }


    public void openMainMapFragment() {
        if (currentState != State.MAIN) {
            openFragmentPage(null, new MainMapOverlayFragment());

            currentState = State.MAIN;
            navMenu.setCheckedItem(R.id.nav_map);
            bottomBarFragment.highlightMainMap();
        }
    }

    public void openDirectionsFragment() {
        if (currentState != State.DIRECTIONS) {
            openFragmentPage(new DirectionsMenuFragment(), null);

            currentState = State.DIRECTIONS;
            navMenu.setCheckedItem(R.id.nav_directions);
            bottomBarFragment.highlightDirections();
        }
    }

    public void openBusesFragment() {
        if (currentState != State.BUSES) {
            openFragmentPage(null, new BusMapOverlayFragment());

            currentState = State.BUSES;
            navMenu.setCheckedItem(R.id.nav_buses);
            bottomBarFragment.highlightBuses();
        }
    }

    public void openBikesFragment() {
        if (currentState != State.BIKES) {
            openFragmentPage(null, new BikesOverlayFragment());

            currentState = State.BIKES;
            navMenu.setCheckedItem(R.id.nav_bikes);
            bottomBarFragment.highlightBikes();
        }
    }

    public void openScheduleFragment() {
        if (currentState != State.SCHEDULE) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            Fragment prevMenuFragment = fragmentManager.findFragmentById(R.id.menu_frame);
            if (prevMenuFragment != null) {
                transaction.remove(prevMenuFragment);
            }

            Fragment bottomBarFragment = fragmentManager.findFragmentById(R.id.bottom_bar_frame);
            if (bottomBarFragment != null) {
                transaction.remove(bottomBarFragment);
            }

            hideMap();

            transaction.replace(R.id.map_overlay_frame, new ScheduleFragment());
            transaction.addToBackStack(ROOT_TAG);
            transaction.commit();

            currentState = State.SCHEDULE;
        }
    }

    public void openSettingsFragment() {
        //Temporarily don't open settings page since it messes up on phones
        //Toast.makeText(getBaseContext(), "This page will be implemented soon!", Toast.LENGTH_SHORT).show();

        if (currentState != State.SETTINGS) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            Fragment prevMenuFragment = fragmentManager.findFragmentById(R.id.menu_frame);
            if (prevMenuFragment != null) {
                transaction.remove(prevMenuFragment);
            }

            Fragment bottomBarFragment = fragmentManager.findFragmentById(R.id.bottom_bar_frame);
            if (bottomBarFragment != null) {
                transaction.remove(bottomBarFragment);
            }

            hideMap();

            transaction.replace(R.id.map_overlay_frame, new SettingsFragment());
            transaction.addToBackStack(ROOT_TAG);
            transaction.commit();

            currentState = State.SETTINGS;
        }
    }

    public void openFaqFragment() {
        if (currentState != State.FAQ) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            Fragment prevMenuFragment = fragmentManager.findFragmentById(R.id.menu_frame);
            if (prevMenuFragment != null) {
                transaction.remove(prevMenuFragment);
            }

            Fragment bottomBarFragment = fragmentManager.findFragmentById(R.id.bottom_bar_frame);
            if (bottomBarFragment != null) {
                transaction.remove(bottomBarFragment);
            }

            hideMap();

            transaction.replace(R.id.map_overlay_frame, new FaqFragment());
            transaction.addToBackStack(ROOT_TAG);
            transaction.commit();

            currentState = State.FAQ;
        }
    }

    public void openFeedbackFragment() {
        if (currentState != State.FEEDBACK) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            Fragment prevMenuFragment = fragmentManager.findFragmentById(R.id.menu_frame);
            if (prevMenuFragment != null) {
                transaction.remove(prevMenuFragment);
            }

            Fragment bottomBarFragment = fragmentManager.findFragmentById(R.id.bottom_bar_frame);
            if (bottomBarFragment != null) {
                transaction.remove(bottomBarFragment);
            }

            hideMap();

            transaction.replace(R.id.map_overlay_frame, new FeedbackFragment());
            transaction.addToBackStack(ROOT_TAG);
            transaction.commit();

            currentState = State.FEEDBACK;
        }
    }

    // Opens the fragments passed in after popping whatever is on the back stack
    public void openFragmentPage(Fragment menuFragment, Fragment overlayFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE); //Pops previous items on Fragment back stack

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //Place overlay fragment if not null
        if (overlayFragment != null) {
            transaction.replace(R.id.map_overlay_frame, overlayFragment);
        }
        //Remove previous overlayFragment if it exists
        else {
            Fragment prevOverlayFragment = fragmentManager.findFragmentById(R.id.map_overlay_frame);
            if (prevOverlayFragment != null) {
                transaction.remove(prevOverlayFragment);
            }
        }
        //Place menu fragment if not null
        if (menuFragment != null) {
            transaction.replace(R.id.menu_frame, menuFragment);
        }
        //Remove previous menu fragment if it exists
        else {
            Fragment prevMenuFragment = fragmentManager.findFragmentById(R.id.menu_frame);
            if (prevMenuFragment != null) {
                transaction.remove(prevMenuFragment);
            }
        }

        showMap();

        transaction.addToBackStack(ROOT_TAG);
        transaction.commit();

        mapFragment.clearMap();
    }

    public void openDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    public void closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create an instance of LOST location engine
            initializeLocationEngine();

            locationPlugin = new LocationLayerPlugin(mapFragment.map, map, locationEngine);
            locationPlugin.setLocationLayerEnabled(LocationLayerMode.COMPASS);
        } else {
            if (prefs.getBoolean("location_preference", true)) {
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(this);
            }
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = new LostLocationEngine(this);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    public Location getLastLocation() {
        if (locationPlugin != null) {
            return locationPlugin.getLastKnownLocation();
        }
        return null;
    }

    public void setCameraPosition(Location location) {
        if (GT_BOUNDS.contains(new LatLng(location.getLatitude(), location.getLongitude()))) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 16));
        }
    }

    private void showMap() {
        if (mapFragment != null) {
            View mapView = mapFragment.getView();
            if (mapView != null) {
                if (mapView.getVisibility() != View.VISIBLE) {
                    mapView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void hideMap() {
        if (mapFragment != null) {
            View mapView = mapFragment.getView();
            if (mapView != null) {
                if (mapView.getVisibility() != View.GONE) {
                    mapView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationPlugin();
        }
        prefs.edit().putBoolean("location_preference", granted).apply(); //Save result in preferences
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            setCameraPosition(location);
            locationEngine.removeLocationEngineListener(this);
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        if (locationPlugin != null) {
            locationPlugin.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    public void passRouteToMap(List<LatLng> points) {
        mapFragment.drawDirectionsRoute(points);
    }

    public void passBusRouteToMap(List<LatLng> points, String routeColor) {
        mapFragment.drawBusesRoute(points, routeColor);
    }

    public void passBusLocationsToMap(List<Bus> points, String routeColor) {
        mapFragment.drawBusLocations(points, routeColor);
    }

    public void passBikeStationsToMap(List<BikeStation> bikeStations) {
        mapFragment.drawBikeStations(bikeStations);
    }

    public void passBusStopsToMap(List<BusStop> busStops, String routeColor) {
        mapFragment.drawBusStops(busStops, routeColor);
    }

    public void passBusStopPredictionsToMap(List<BusStop> busStopPredictions) {
        mapFragment.updateBusStops(busStopPredictions);
    }

    public void clearBuses() {
        mapFragment.clearBusesAndStops();
    }
}
