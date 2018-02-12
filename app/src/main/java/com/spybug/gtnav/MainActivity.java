package com.spybug.gtnav;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.maps.UiSettings;

import static com.spybug.gtnav.HelperUtil.convertDpToPixel;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MapboxMap map;

    private static final LatLngBounds GT_BOUNDS = new LatLngBounds.Builder()
            .include(new LatLng(33.753312, -84.421579))
            .include(new LatLng(33.797474, -84.372656))
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment;
        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            LatLng GT = new LatLng(33.7756, -84.3963);

            MapboxMapOptions options = new MapboxMapOptions();
            options.styleUrl(Style.MAPBOX_STREETS);
            options.camera(new CameraPosition.Builder()
                    .target(GT)
                    .zoom(14)
                    .build());

            mapFragment = SupportMapFragment.newInstance(options);
            Fragment bottomBarFragment = new BottomNavbarFragment();
            Fragment mainMapOverlayFragment = new MainMapOverlayFragment();

            transaction.add(R.id.map_frame, mapFragment, "com.mapbox.map");
            transaction.add(R.id.bottom_bar_frame, bottomBarFragment, "com.gtnav.bottomBar");
            transaction.add(R.id.map_overlay_frame, mainMapOverlayFragment, "com.gtnav.mainMapOverlay");
            transaction.commit();
        } else {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
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
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            Fragment directionsMenu = new DirectionsMenuFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.directions_menu_frame, directionsMenu)
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment newFragment = null;

        if (id == R.id.nav_directions) {
            newFragment = new DirectionsFragment();
        } else if (id == R.id.nav_buses) {
            newFragment = new BusesFragment();
        } else if (id == R.id.nav_bikes) {
            newFragment = new BikesFragment();
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

//        try {
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            Fragment curFragment = fragmentManager.findFragmentById(R.id.content_frame);
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//            //if its a new page, replace it
//            if (!curFragment.getClass().equals(newFragment.getClass())) {
//                fragmentTransaction.replace(R.id.content_frame, newFragment);
//                if (curFragment instanceof MainFragment) {
//                    fragmentTransaction.addToBackStack(null); //allow back button to lead back to MainFragment if leaving it
//                }
//                fragmentTransaction.commit();
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        closeDrawer();
        return true;
    }

    public void uncheckAllMenuItems() {
        final Menu menu = ((NavigationView) findViewById(R.id.nav_view)).getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            item.setChecked(false);
        }
    }

    public void openDirectionsFragment(boolean addPrevToStack) {
        Fragment directionsFragment = new DirectionsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.replace(R.id.content_frame, directionsFragment);
        if (addPrevToStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

        NavigationView menu = findViewById(R.id.nav_view);
        menu.setCheckedItem(R.id.nav_directions);
    }

    public void openBusesFragment(boolean addPrevToStack) {
        Fragment busesFragment = new BusesFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.replace(R.id.content_frame, busesFragment);
        if (addPrevToStack) {
                fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

        NavigationView menu = findViewById(R.id.nav_view);
        menu.setCheckedItem(R.id.nav_buses);
    }

    public void openBikesFragment(boolean addPrevToStack) {
        Fragment bikesFragment = new BikesFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.replace(R.id.content_frame, bikesFragment);
        if (addPrevToStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

        NavigationView menu = findViewById(R.id.nav_view);
        menu.setCheckedItem(R.id.nav_bikes);
    }

    public void openDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    public void closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}
