package com.spybug.gtnav.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.spybug.gtnav.models.BikeStation;
import com.spybug.gtnav.utils.BikeStationsServerRequest;
import com.spybug.gtnav.interfaces.Communicator;
import com.spybug.gtnav.interfaces.OnEventListener;
import com.spybug.gtnav.R;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainMapOverlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainMapOverlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BikesOverlayFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private ImageButton menuButton;
    private View view;

    public BikesOverlayFragment() {
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
    public static BikesOverlayFragment newInstance(String param1, String param2) {
        BikesOverlayFragment fragment = new BikesOverlayFragment();
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
        view = inflater.inflate(R.layout.fragment_bikes_overlay, container, false);
        final Switch stationSwitch = view.findViewById(R.id.bike_station_switch);
        stationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!stationSwitch.isChecked()) {
                    hideBikeStations();
                    stationSwitch.setText(getString(R.string.show_station_switch_text));
                } else {
                    showBikeStations();
                    stationSwitch.setText(getString(R.string.show_racks_switch_text));
                }
            }
        });

        getBikeStations();


        return view;
    }

    public void getBikeStations() {
        new BikeStationsServerRequest(getContext(), new OnEventListener<List<BikeStation>, String>() {
            @Override
            public void onSuccess(List<BikeStation> bikeStations) {
                Communicator communicator = (Communicator) getActivity();
                if (communicator != null) {
                    communicator.passBikeStationsToMap(bikeStations);
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show();
            }
        }).execute();
    }

    /**
     * Hides the bike stations on the map and displays the bike rack locations
     */
    private void hideBikeStations() {
        //TODO
    }

    /**
     * Hides the bike rack locations on the map and shows the bike stations
     */
    private void showBikeStations() {
        //TODO
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
