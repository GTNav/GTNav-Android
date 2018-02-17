package com.spybug.gtnav;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.android.telemetry.location.LocationEngine;


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
    private EditText startLocation, endLocation;
    private ImageButton walkingButton, busesButton, bikingButton;
    private boolean directionsRequested = false;
    private View v;

    private enum SelectedMode {
        WALKING("walking"),
        BUSES("driving"), //change to buses after bus routing setup
        BIKING("cycling");

        private final String text;

        SelectedMode(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
    private SelectedMode curSelectedMode;

    private static final int transparent = Color.argb(0,0,0,0);

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
        v = inflater.inflate(R.layout.fragment_directions_menu, container, false);
        final View myView = v;

        startLocation = v.findViewById(R.id.start_location);
        endLocation = v.findViewById(R.id.end_location);
        walkingButton = v.findViewById(R.id.mode_walking_button);
        busesButton = v.findViewById(R.id.mode_buses_button);
        bikingButton = v.findViewById(R.id.mode_biking_button);

        //simulate back button being pressed
        v.findViewById(R.id.directions_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).onBackPressed();
            }
        });


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
                            if (directionsRequested) {
                                makeDirectionsRequest();
                            }
                        }
                        break;
                    case R.id.mode_buses_button:
                        if (curSelectedMode != SelectedMode.BUSES) {
                            modeChanged(SelectedMode.BUSES);
                            curSelectedMode = SelectedMode.BUSES;
                            if (directionsRequested) {
                                makeDirectionsRequest();
                            }
                        }
                        break;
                    case R.id.mode_biking_button:
                        if (curSelectedMode != SelectedMode.BIKING) {
                            modeChanged(SelectedMode.BIKING);
                            curSelectedMode = SelectedMode.BIKING;
                            if (directionsRequested) {
                                makeDirectionsRequest();
                            }
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

        checkLocation();

        EditText.OnEditorActionListener locationEditTextListener = new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (startLocation.getText().length() == 0 || endLocation.getText().length() == 0) {
                        return true;
                    }

                    //hide the keyboard
                    InputMethodManager imm = (InputMethodManager) myView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    if (!directionsRequested) {
                        makeDirectionsRequest();
                    }
                    return false;
                } else {
                    return false;
                }

            }
        };

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    directionsRequested = (count - before == 0) && directionsRequested;
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };

        startLocation.setOnEditorActionListener(locationEditTextListener);
        startLocation.addTextChangedListener(textWatcher);
        endLocation.setOnEditorActionListener(locationEditTextListener);
        endLocation.addTextChangedListener(textWatcher);

        return v;
    }

    private void makeDirectionsRequest() {
        Location location = ((MainActivity) getActivity()).getLastLocation();

        if (endLocation != null && startLocation != null) {
            try {
                DirectionsServerRequest req = new DirectionsServerRequest(v.getContext(), new OnEventListener<LatLng[], String>() {
                    @Override
                    public void onSuccess(LatLng[] points) {
                        ((Communicator) getActivity()).passRouteToMap(points);
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(v.getContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
                req.execute(startLocation.getText().toString(),
                        endLocation.getText().toString(),
                        curSelectedMode.toString(),
                        location,
                        getString(R.string.mapbox_key));

                directionsRequested = true;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
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

    private void checkLocation() {
        Location lastLocation = ((MainActivity) getActivity()).getLastLocation();

        if (lastLocation != null) {
            startLocation.setText(getString(R.string.current_location));
            ((MainActivity) getActivity()).setCameraPosition(lastLocation);
            endLocation.requestFocus();
        }
        else {
            startLocation.requestFocus();
        }
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
