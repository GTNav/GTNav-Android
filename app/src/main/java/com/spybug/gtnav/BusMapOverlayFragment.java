package com.spybug.gtnav;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

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
    // TODO: Rename and change types and number of parameters
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
