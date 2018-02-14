package com.spybug.gtnav;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapbox.mapboxsdk.geometry.LatLng;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DirectionsMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectionsMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectionsMenuFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private Communicator mCommunicator;
    private EditText startLocation, endLocation;
    private ImageButton walkingButton, busesButton, bikingButton;

    private enum SelectedMode {WALKING, BUSES, BIKING};
    private SelectedMode curSelectedMode;

    private final int transparent = Color.argb(0,0,0,0);

    public DirectionsMenuFragment() {
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
    public static DirectionsMenuFragment newInstance(String param1, String param2) {
        DirectionsMenuFragment fragment = new DirectionsMenuFragment();
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
        View v = inflater.inflate(R.layout.fragment_directions_menu, container, false);
        final View myView = v;

        startLocation = v.findViewById(R.id.start_location);
        endLocation = v.findViewById(R.id.end_location);
        walkingButton = v.findViewById(R.id.mode_walking_button);
        busesButton = v.findViewById(R.id.mode_buses_button);
        bikingButton = v.findViewById(R.id.mode_biking_button);

        curSelectedMode = SelectedMode.WALKING; //Default to walking, but should load from user prefs/last value
        modeChanged(curSelectedMode);

        View.OnClickListener modeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageButton clickedButton = (ImageButton) view;
                switch (clickedButton.getId()) {
                    case R.id.mode_walking_button:
                        if (curSelectedMode != SelectedMode.WALKING) {
                            modeChanged(SelectedMode.WALKING);
                            curSelectedMode = SelectedMode.WALKING;
                        }
                        break;
                    case R.id.mode_buses_button:
                        if (curSelectedMode != SelectedMode.BUSES) {
                            modeChanged(SelectedMode.BUSES);
                            curSelectedMode = SelectedMode.BUSES;
                        }
                        break;
                    case R.id.mode_biking_button:
                        if (curSelectedMode != SelectedMode.BIKING) {
                            modeChanged(SelectedMode.BIKING);
                            curSelectedMode = SelectedMode.BIKING;
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        walkingButton.setOnClickListener(modeClickListener);
        busesButton.setOnClickListener(modeClickListener);
        bikingButton.setOnClickListener(modeClickListener);

        endLocation.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (startLocation.getText().length() == 0 || endLocation.getText().length() == 0) {
                        return true;
                    }

                    //hide the keyboard
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    try {
                        LatLng[] points = (LatLng[]) new DirectionsServerRequest(v.getContext()).execute(myView, getString(R.string.mapbox_key)).get();
                        ((Communicator) getActivity()).passRouteToMap(points);

                        return false;
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                else {
                    return false;
                }
            }
        });



        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

    //Updates UI to select newMode
    private void modeChanged(SelectedMode newMode) {
        switch (newMode) {
            case WALKING:
                resetImageButton(bikingButton);
                resetImageButton(busesButton);
                focusImageButton(walkingButton);
                break;
            case BUSES:
                resetImageButton(walkingButton);
                resetImageButton(bikingButton);
                focusImageButton(busesButton);
                break;
            case BIKING:
                resetImageButton(walkingButton);
                resetImageButton(busesButton);
                focusImageButton(bikingButton);
                break;
            default:
                break;
        }
    }

    private void resetImageButton(ImageButton imageButton) {
        imageButton.setBackgroundColor(transparent);
        imageButton.setColorFilter(getResources().getColor(R.color.white));
    }

    private void focusImageButton(ImageButton imageButton) {
        imageButton.setBackground(getResources().getDrawable(R.drawable.round_button_white));
        imageButton.setColorFilter(getResources().getColor(R.color.directionsBar));
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
