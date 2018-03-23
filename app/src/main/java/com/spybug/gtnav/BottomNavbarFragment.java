package com.spybug.gtnav;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BottomNavbarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class BottomNavbarFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ImageButton directionsButton, busesButton, bikesButton, mapButton;
    private TextView directionsText, busesText, bikesText, mapText;

    public BottomNavbarFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_bottom_navbar, container, false);

        mapButton = v.findViewById(R.id.map_button);
        mapText = v.findViewById(R.id.map_button_text);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                {
                    activity.openMainMapFragment(); //opens main map page
                }
            }
        });

        directionsButton = v.findViewById(R.id.directions_button);
        directionsText = v.findViewById(R.id.directions_button_text);
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                {
                    activity.openDirectionsFragment(); //opens directions page
                }
            }
        });

        busesButton = v.findViewById(R.id.buses_button);
        busesText = v.findViewById(R.id.buses_button_text);
        busesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                {
                    activity.openBusesFragment(); //opens buses page
                }
            }
        });

        bikesButton = v.findViewById(R.id.bikes_button);
        bikesText = v.findViewById(R.id.bikes_button_text);
        bikesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                {
                    activity.openBikesFragment(); //opens bikes page
                }
            }
        });

        highlightMainMap(); //Default to main map

        return v;
    }

    public void highlightMainMap() {
        if (mapText != null) {
            mapText.setTypeface(Typeface.DEFAULT_BOLD);
            mapButton.setElevation(6);

            directionsText.setTypeface(Typeface.DEFAULT);
            directionsButton.setElevation(0);

            busesText.setTypeface(Typeface.DEFAULT);
            busesButton.setElevation(0);

            bikesText.setTypeface(Typeface.DEFAULT);
            bikesButton.setElevation(0);
        }
    }

    public void highlightDirections() {
        if (mapText != null) {
            mapText.setTypeface(Typeface.DEFAULT);
            mapButton.setElevation(0);

            directionsText.setTypeface(Typeface.DEFAULT_BOLD);
            directionsButton.setElevation(6);

            busesText.setTypeface(Typeface.DEFAULT);
            busesButton.setElevation(0);

            bikesText.setTypeface(Typeface.DEFAULT);
            bikesButton.setElevation(0);
        }
    }

    public void highlightBuses() {
        if (mapText != null) {
            mapText.setTypeface(Typeface.DEFAULT);
            mapButton.setElevation(0);

            directionsText.setTypeface(Typeface.DEFAULT);
            directionsButton.setElevation(0);

            busesText.setTypeface(Typeface.DEFAULT_BOLD);
            busesButton.setElevation(6);

            bikesText.setTypeface(Typeface.DEFAULT);
            bikesButton.setElevation(0);
        }
    }

    public void highlightBikes() {
        if (mapText != null) {
            mapText.setTypeface(Typeface.DEFAULT);
            mapButton.setElevation(0);

            directionsText.setTypeface(Typeface.DEFAULT);
            directionsButton.setElevation(0);

            busesText.setTypeface(Typeface.DEFAULT);
            busesButton.setElevation(0);

            bikesText.setTypeface(Typeface.DEFAULT_BOLD);
            bikesButton.setElevation(6);
        }
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
