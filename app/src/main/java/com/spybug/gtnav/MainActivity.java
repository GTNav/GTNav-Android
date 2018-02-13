package com.spybug.gtnav;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.List;

import static com.spybug.gtnav.HelperUtil.convertDpToPixel;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationEngineListener, PermissionsListener, Communicator {

    private MapboxMap map;
    private MapFragment mapFragment;
    private NavigationView navMenu;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private State currentState;

    private static final LatLngBounds GT_BOUNDS = new LatLngBounds.Builder()
            .include(new LatLng(33.753312, -84.421579))
            .include(new LatLng(33.797474, -84.372656))
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentState = State.MAIN;

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            LatLng GT = new LatLng(33.7756, -84.3963);

            MapboxMapOptions options = new MapboxMapOptions();
            options.styleUrl(Style.MAPBOX_STREETS);
            options.camera(new CameraPosition.Builder()
                    .target(GT)
                    .zoom(14)
                    .build());

            mapFragment = MapFragment.newInstance(options);
            Fragment bottomBarFragment = new BottomNavbarFragment();
            Fragment mainMapOverlayFragment = new MainMapOverlayFragment();

            transaction.add(R.id.map_frame, mapFragment, "com.mapbox.map");
            transaction.add(R.id.bottom_bar_frame, bottomBarFragment, "com.gtnav.bottomBar");
            transaction.add(R.id.map_overlay_frame, mainMapOverlayFragment, "com.gtnav.mainMapOverlay");
            transaction.commit();
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

        navMenu = findViewById(R.id.nav_view);
        navMenu.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment newFragment = null;

        if (id == R.id.nav_directions) {
            newFragment = new DirectionsMenuFragment();
        } else if (id == R.id.nav_buses) {
            newFragment = new ScheduleFragment();
        } else if (id == R.id.nav_bikes) {
            newFragment = new ScheduleFragment();
        } else if (id == R.id.nav_schedule) {
            newFragment = new ScheduleFragment();
        } else if (id == R.id.nav_settings) {
            newFragment = new ScheduleFragment();
        } else if (id == R.id.nav_faq) {
            newFragment = new ScheduleFragment();
        } else if (id == R.id.nav_feedback) {
            newFragment = new ScheduleFragment();
        } else {
            newFragment = new ScheduleFragment();
        }

        try {
            if (currentState == State.MAIN) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Fragment overlayFragment = fragmentManager.findFragmentById(R.id.map_overlay_frame);
                fragmentTransaction.remove(overlayFragment);
                fragmentTransaction.replace(R.id.menu_frame, newFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        closeDrawer();
        item.setChecked(true);
        return true;
    }

    public void resetMainState() {
        Menu menu = navMenu.getMenu();
        if (navMenu != null) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.isChecked()) {
                    item.setChecked(false);
                }
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment menuFragment = fragmentManager.findFragmentById(R.id.menu_frame);
        if (menuFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(menuFragment)
                    .commit();
        }
        mapFragment.clearMap();
        currentState = State.MAIN;
    }

    public void openDirectionsMenuFragment() {
        if (currentState != State.DIRECTIONS) {
            Fragment fragment = new DirectionsMenuFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.menu_frame, fragment);
            if (currentState == State.MAIN) {
                Fragment overlayFragment = fragmentManager.findFragmentById(R.id.map_overlay_frame);
                fragmentTransaction.remove(overlayFragment);
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
            currentState = State.DIRECTIONS;

            navMenu.setCheckedItem(R.id.nav_directions);
        }
    }

    public void openBusesFragment() {
        if (currentState != State.BUSES) {
            Fragment fragment = new ScheduleFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.menu_frame, fragment);
            if (currentState == State.MAIN) {
                Fragment overlayFragment = fragmentManager.findFragmentById(R.id.map_overlay_frame);
                fragmentTransaction.remove(overlayFragment);
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
            currentState = State.BUSES;

            navMenu.setCheckedItem(R.id.nav_buses);
        }
    }

    public void openBikesFragment() {
        if (currentState != State.BIKES) {
            Fragment fragment = new ScheduleFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.menu_frame, fragment);
            if (currentState == State.MAIN) {
                Fragment overlayFragment = fragmentManager.findFragmentById(R.id.map_overlay_frame);
                fragmentTransaction.remove(overlayFragment);
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
            currentState = State.BIKES;

            navMenu.setCheckedItem(R.id.nav_bikes);
        }
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
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
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

    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 16));
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
        } else {
            Toast.makeText(this, "You didn't grant location permissions.", Toast.LENGTH_LONG).show();
            finish();
        }
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

    @Override
    public void passRouteToMap(LatLng[] points) {
        mapFragment.drawDirectionsRoute(points);
    }
}
