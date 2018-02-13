package com.spybug.gtnav;

import android.content.Context;
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

        startLocation = v.findViewById(R.id.start_location);
        endLocation = v.findViewById(R.id.end_location);
        final View myView = v;

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
