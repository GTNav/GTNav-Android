package com.spybug.gtnav;

import android.content.Context;
import android.content.res.ColorStateList;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BusMapOverlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BusMapOverlayFragment#newInstance} factory method to
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
    private enum CurrentRoute {RED, BLUE, GREEN, TROLLEY, MIDNIGHT, EXPRESS}
    private CurrentRoute currentRoute;

    public BusMapOverlayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DirectionsFragment.
     */
    public static BusMapOverlayFragment newInstance(String param1, String param2) {
        BusMapOverlayFragment fragment = new BusMapOverlayFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bus_map_overlay, container, false);

        final String routeColor = "red";

        new BusRouteServerRequest(view.getContext(), new OnEventListener<List<LatLng>, String>() {
            @Override
            public void onSuccess(List<LatLng> route) {
                ((Communicator) getActivity()).passBusRouteToMap(route, routeColor);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
            }
        }).execute();

        fabSelect = view.findViewById(R.id.fabSelect);
        FloatingActionButton fabRed =  view.findViewById(R.id.fabRed); //initial

        List<FloatingActionButton> fabButtons = new LinkedList<>();
        fabButtons.add((FloatingActionButton) fabRed);
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
            }
        };

        for (FloatingActionButton fab : fabButtons) {
            fab.setOnClickListener(fabColorOnClickListener);
        }

        fabSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded){
                    closeSubMenusFab(ColorStateList.valueOf(Color.parseColor("#ffffff")));
                } else {
                    openSubMenusFab();
                }
            }
        });

        currentRoute = CurrentRoute.RED; //Default route, will be set based on saved last viewed route later
        closeSubMenusFab(fabRed.getBackgroundTintList());

        return view;
    }

    private Runnable busUpdater = new Runnable() {
        @Override
        public void run() {
            getBusLocations("red");
            //Toast.makeText(view.getContext(), "Making bus request", Toast.LENGTH_SHORT).show(); //For debugging to tell when bus location updated
            handler.postDelayed(busUpdater, busDelayms);
        }
    };

    private void getBusLocations(String routeColor) {
        final String fRouteColor = routeColor;

        new BusLocationsServerRequest(view.getContext(), new OnEventListener<List<LatLng>, String>() {
            @Override
            public void onSuccess(List<LatLng> buses) {
                ((Communicator) getActivity()).passBusLocationsToMap(buses, fRouteColor);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
            }
        }).execute(fRouteColor);
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

        getBusLocations("red");
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
