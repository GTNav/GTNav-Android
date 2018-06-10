package com.spybug.gtnav.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.spybug.gtnav.R;
import com.spybug.gtnav.activities.MainActivity;
import com.spybug.gtnav.models.AppDatabase;
import com.spybug.gtnav.models.HeaderItem;
import com.spybug.gtnav.models.ListItem;
import com.spybug.gtnav.models.ScheduleEvent;
import com.spybug.gtnav.models.ScheduleEventAdapter;

import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScheduleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment implements AddScheduleEventFragment.AddEventDialogListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private RecyclerView recyclerView;
    private List<ListItem> listItems = new ArrayList<>();
    private Map<Date, List<ScheduleEvent>> eventsMap;

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
        FloatingActionButton addFab = v.findViewById(R.id.AddToScheduleFAB);

        List<ScheduleEvent> events = AppDatabase.getAppDatabase(getContext()).scheduleEventDao().getAll();

        eventsMap = toMap(events);

        for (Date date : eventsMap.keySet()) {
            HeaderItem header = new HeaderItem(date);
            listItems.add(header);
            listItems.addAll(eventsMap.get(date));
        }

        recyclerView = v.findViewById(R.id.schedule_recyclerview);
        recyclerView.setAdapter(new ScheduleEventAdapter(listItems));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        return v;
    }

    private Map<Date, List<ScheduleEvent>> toMap(List<ScheduleEvent> events) {
        Map<Date, List<ScheduleEvent>> map = new TreeMap<>();
        for (ScheduleEvent event : events) {
            Date date = calendarToDate(event.getTime());

            List<ScheduleEvent> val = map.get(date);
            if (val == null) {
                val = new ArrayList<>();
                map.put(date, val);
            }
            val.add(event);
        }

        return map;
    }

    private Date calendarToDate(Calendar calendar) {
        Calendar copy = (Calendar) calendar.clone();

        copy.set(Calendar.MILLISECOND, 0);
        copy.set(Calendar.SECOND, 0);
        copy.set(Calendar.MINUTE, 0);
        copy.set(Calendar.HOUR_OF_DAY, 0);

        return copy.getTime();
    }

    private void openDialog() {
        DialogFragment addEventFragment = new AddScheduleEventFragment();
        addEventFragment.setTargetFragment(this, 1);
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            addEventFragment.show(activity.getSupportFragmentManager(), "AddScheduleEvent");
        }
    }

    // The dialog fragment sends back its finished object
    public void onDialogPositiveClick(List<ScheduleEvent> events) {
        Toast.makeText(getContext(), "Saved " + events.size() + " new events to db", Toast.LENGTH_LONG).show();
        //eventList.addAll(events);
        AppDatabase.getAppDatabase(getContext()).scheduleEventDao().insertAll(events);
        //TODO: Add items to eventsList in proper spot based on dates.

        //listAdapter.notifyDataSetChanged();
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

