package com.spybug.gtnav.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.spybug.gtnav.R;
import com.spybug.gtnav.models.AppDatabase;
import com.spybug.gtnav.models.ScheduleEvent;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.SimpleTimeZone;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScheduleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    ArrayAdapter<ScheduleEvent> listAdapter;
    private ListView scheduleView;
    private List<ScheduleEvent> eventList;

    public ScheduleFragment() {
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
    public static ScheduleFragment newInstance(String param1, String param2) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);

        eventList = new ArrayList<>();
        scheduleView = v.findViewById(R.id.ScheduleListView);
        FloatingActionButton addFab = v.findViewById(R.id.AddToScheduleFAB);

        listAdapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1, eventList);
        scheduleView.setAdapter(listAdapter);

        List<ScheduleEvent> events = AppDatabase.getAppDatabase(getContext()).scheduleEventDao().getAll();
        eventList.addAll(events);

        scheduleView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
                Toast.makeText(ScheduleFragment.this.getContext(), "Event Selected", Toast.LENGTH_SHORT).show();
            }
        });

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScheduleEvent newEvent = addEvent();
                eventList.add(newEvent);
                AppDatabase.getAppDatabase(getContext()).scheduleEventDao().insert(newEvent);

                listAdapter.notifyDataSetChanged();
                Toast.makeText(v.getContext(), "Event Added", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    private ScheduleEvent addEvent() {
        DialogFragment addEventFragment = new AddScheduleEventFragment();
        addEventFragment.show(getActivity().getSupportFragmentManager(), "AddScheduleEvent");
        return new ScheduleEvent("test name " + Math.random(), Math.random(), "location name", new LatLng(), new GregorianCalendar());
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    public void onDialogPositiveClick(DialogFragment dialog) {
        Toast.makeText(getContext(), "Sure thing my dude", Toast.LENGTH_LONG).show();
    }

    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
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

