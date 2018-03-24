package com.spybug.gtnav.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.spybug.gtnav.models.Bus;
import com.spybug.gtnav.models.BusStop;
import com.spybug.gtnav.utils.BusLocationsServerRequest;
import com.spybug.gtnav.utils.BusRouteServerRequest;
import com.spybug.gtnav.interfaces.Communicator;
import com.spybug.gtnav.interfaces.OnEventListener;
import com.spybug.gtnav.R;
import com.spybug.gtnav.utils.BusStopServerRequest;

import java.util.LinkedList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BusMapOverlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BusMapOverlayFragment} factory method to
 * create an instance of this fragment.
 */
public class BusMapOverlayFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private ImageButton menuButton;
    private View view;
    private Handler handler;
    private final int busDelayms = 15000; //15 seconds

    private boolean fabExpanded = false;
    private FloatingActionButton fabSelect;
    private LinearLayout layoutFabBlue, layoutFabRed, layoutFabGreen, layoutFabTrolley, layoutFabMidnight, layoutFabExpress;

    private enum CurrentRoute {
        RED("red"),
        BLUE("blue"),
        GREEN("green"),
        TROLLEY("trolley"),
        MIDNIGHT("midnight"),
        EXPRESS("express");

        private final String text;

        CurrentRoute(final String text) {
            this.text = text;
        }

        public String getColor(Context context) {
            Resources res = context.getResources();
            switch (this.name()) {
                case "RED":
                    return res.getString(R.color.redRoute);
                case "BLUE":
                    return res.getString(R.color.blueRoute);
                case "GREEN":
                    return res.getString(R.color.greenRoute);
                case "TROLLEY":
                    return res.getString(R.color.techTrolley);
                case "EXPRESS":
                    return res.getString(R.color.techExpress);
                case "MIDNIGHT":
                    return res.getString(R.color.midnightRambler);
                default:
                    return "#FFFFFF";
            }
        }

        @Override
        public String toString() {
            return text;
        }
    }
    private CurrentRoute currentRoute;

    public BusMapOverlayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getResources().getColor(R.color.redRoute);
        currentRoute = CurrentRoute.RED; //default
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bus_map_overlay, container, false);

        getBusRoute(currentRoute);
        getBusStops(currentRoute);

        fabSelect = view.findViewById(R.id.fabSelect);

        final List<FloatingActionButton> fabButtons = new LinkedList<>();
        fabButtons.add((FloatingActionButton) view.findViewById(R.id.fabRed));
        fabButtons.add((FloatingActionButton) view.findViewById(R.id.fabBlue));
        fabButtons.add((FloatingActionButton) view.findViewById(R.id.fabGreen));
        fabButtons.add((FloatingActionButton) view.findViewById(R.id.fabMidnight));
        fabButtons.add((FloatingActionButton) view.findViewById(R.id.fabExpress));
        fabButtons.add((FloatingActionButton) view.findViewById(R.id.fabTrolley));

        layoutFabBlue = view.findViewById(R.id.layoutFabBlue);
        layoutFabRed = view.findViewById(R.id.layoutFabRed);
        layoutFabGreen = view.findViewById(R.id.layoutFabGreen);
        layoutFabTrolley = view.findViewById(R.id.layoutFabTrolley);
        layoutFabMidnight = view.findViewById(R.id.layoutFabMidnight);
        layoutFabExpress = view.findViewById(R.id.layoutFabExpress);

        View.OnClickListener fabColorOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FloatingActionButton fabView = (FloatingActionButton) view;
                final int fabId = fabView.getId();

                CurrentRoute prevRoute = currentRoute;
                switch(fabId) {
                    case R.id.fabBlue:
                        currentRoute = CurrentRoute.BLUE;
                        break;
                    case R.id.fabRed:
                        currentRoute = CurrentRoute.RED;
                        break;
                    case R.id.fabGreen:
                        currentRoute = CurrentRoute.GREEN;
                        break;
                    case R.id.fabTrolley:
                        currentRoute = CurrentRoute.TROLLEY;
                        break;
                    case R.id.fabMidnight:
                        currentRoute = CurrentRoute.MIDNIGHT;
                        break;
                    case R.id.fabExpress:
                        currentRoute = CurrentRoute.EXPRESS;
                        break;
                    default:
                        break;
                }
                closeSubMenusFab(fabView.getBackgroundTintList());
                //if user changed current route
                if (prevRoute != currentRoute) {
                    handler.removeCallbacks(busUpdater);
                    ((Communicator) getActivity()).clearBuses();
                    getBusRoute(currentRoute);
                    getBusStops(currentRoute);
                    getBusLocations(currentRoute);
                    handler.postDelayed(busUpdater, busDelayms);
                }
            }
        };

        for (FloatingActionButton fab : fabButtons) {
            fab.setOnClickListener(fabColorOnClickListener);
        }

        fabSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded){
                    ColorStateList color = ColorStateList.valueOf(
                            Color.parseColor(currentRoute.getColor(getContext())));

                    closeSubMenusFab(color);
                } else {
                    openSubMenusFab();
                }
            }
        });

        currentRoute = CurrentRoute.RED; //Default route, will be set based on saved last viewed route later
        closeSubMenusFab(ColorStateList.valueOf(
                Color.parseColor(currentRoute.getColor(getContext()))));

        return view;
    }

    private Runnable busUpdater = new Runnable() {
        @Override
        public void run() {
            getBusLocations(currentRoute);
            //Toast.makeText(view.getContext(), "Making bus request", Toast.LENGTH_SHORT).show(); //For debugging to tell when bus location updated
            handler.postDelayed(busUpdater, busDelayms);
        }
    };

    private void getBusLocations(CurrentRoute route) {
        final String fRouteName = route.toString();
        final String fRouteColor = route.getColor(getContext());

        new BusLocationsServerRequest(view.getContext(), new OnEventListener<List<Bus>, String>() {
            @Override
            public void onSuccess(List<Bus> buses) {
                Communicator communicator = (Communicator) getActivity();
                if (communicator != null) {
                    communicator.passBusLocationsToMap(buses, fRouteColor);
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
            }
        }).execute(fRouteName);
    }

    private void getBusRoute(CurrentRoute route) {
        final String fRouteName = route.toString();
        final String fRouteColor = route.getColor(getContext());

        new BusRouteServerRequest(view.getContext(), new OnEventListener<List<List<LatLng>>, String>() {
            @Override
            public void onSuccess(List<List<LatLng>> route) {
                Communicator communicator = (Communicator) getActivity();
                if (communicator != null) {
                    communicator.passBusRouteToMap(route, fRouteColor);
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
            }
        }).execute(fRouteName);
    }

    public void getBusStops(CurrentRoute route) {
        final String fRouteName = route.toString();
        final String fRouteColor = route.getColor(getContext());

        new BusStopServerRequest(view.getContext(), new OnEventListener<List<BusStop>, String>() {
            @Override
            public void onSuccess(List<BusStop> stops) {
                Communicator communicator = (Communicator) getActivity();
                if (communicator != null) {
                    communicator.passBusStopsToMap(stops, fRouteColor);
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
            }
        }).execute(fRouteName);
    }

    //closes FAB submenus
    private void closeSubMenusFab(ColorStateList routeColor){
        layoutFabBlue.setVisibility(View.INVISIBLE);
        layoutFabRed.setVisibility(View.INVISIBLE);
        layoutFabGreen.setVisibility(View.INVISIBLE);
        layoutFabTrolley.setVisibility(View.INVISIBLE);
        layoutFabMidnight.setVisibility(View.INVISIBLE);
        layoutFabExpress.setVisibility(View.INVISIBLE);
        fabSelect.setBackgroundTintList(routeColor);
        fabSelect.setImageResource(R.drawable.ic_bus_black);
        fabExpanded = false;
    }

    //Opens FAB submenus
    private void openSubMenusFab(){
        layoutFabBlue.setVisibility(View.VISIBLE);
        layoutFabRed.setVisibility(View.VISIBLE);
        layoutFabGreen.setVisibility(View.VISIBLE);
        layoutFabTrolley.setVisibility(View.VISIBLE);
        layoutFabMidnight.setVisibility(View.VISIBLE);
        layoutFabExpress.setVisibility(View.VISIBLE);
        fabSelect.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff")));
        fabSelect.setImageResource(R.drawable.ic_close_black);
        fabExpanded = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        if (handler != null) {
            handler.removeCallbacks(busUpdater); //removes all callbacks
            handler = null;
        }
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        if (handler != null) {
            handler.removeCallbacks(busUpdater); //removes all callbacks
            handler = null;
        }
        super.onDestroyView();

    }

    @Override
    public void onStop() {
        if (handler != null) {
            handler.removeCallbacks(busUpdater); //removes all callbacks
            handler = null;
        }
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (handler == null) {
            handler = new Handler();
        }

        getBusLocations(currentRoute);
        handler.postDelayed(busUpdater, busDelayms);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
