package com.spybug.gtnav.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.spybug.gtnav.R;
import com.spybug.gtnav.models.ScheduleEvent;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 *
 */
public class AddScheduleEventFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ScheduleEvent event;

    AddEventDialogListener mListener;

    public AddScheduleEventFragment() {
        // Required empty public constructor
    }

    public interface AddEventDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddScheduleEventFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddScheduleEventFragment newInstance(String param1, String param2) {
        AddScheduleEventFragment fragment = new AddScheduleEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.schedule_addevent_dialog, null);
        builder.setView(v)
                .setCancelable(true)
                .setPositiveButton(R.string.submit_creation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                })
                .setNegativeButton(R.string.cancel_creation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });

        final EditText timeEdit = v.findViewById(R.id.time);
        timeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GregorianCalendar currentTime = (GregorianCalendar) GregorianCalendar.getInstance();

                //TODO: get time from stored ScheduleEvent value if there, otherwise use current
                final int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                TimePickerDialog timePicker;
                timePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        //TODO: Update ScheduleEvent object with new time

                        String timeString = timeTo12HR(hourOfDay, minute);
                        timeEdit.setText(timeString);
                    }
                }, hour, minute, false);
                timePicker.setTitle("Select time");
                timePicker.show();
            }
        });

        EditText startDateEdit = v.findViewById(R.id.start_date);
        EditText endDateEdit = v.findViewById(R.id.end_date);
        startDateEdit.setOnClickListener(dateEditClick);
        endDateEdit.setOnClickListener(dateEditClick);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private EditText.OnClickListener dateEditClick = new EditText.OnClickListener() {
        @Override
        public void onClick(final View view) {
            GregorianCalendar currentDate = (GregorianCalendar) GregorianCalendar.getInstance();

            DatePickerDialog datePicker;
            datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    //TODO: Update ScheduleEvent object with new date
                    GregorianCalendar dateResult = (GregorianCalendar) GregorianCalendar.getInstance();
                    dateResult.set(year, month, day);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
                    String dateString = dateFormat.format(dateResult.getTime());
                    ((EditText) view).setText(dateString);
                }
            }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
            datePicker.setTitle("Select Date");
            datePicker.show();
        }
    };

    private String timeTo12HR(int hourOfDay, int minute) {
        String am_pm;

        Calendar datetime = Calendar.getInstance();
        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        datetime.set(Calendar.MINUTE, minute);

        if (datetime.get(Calendar.AM_PM) == Calendar.AM)
            am_pm = "AM";
        else
            am_pm = "PM";

        String strHrsToShow = (datetime.get(Calendar.HOUR) == 0) ? "12" : datetime.get(Calendar.HOUR)+"";
        return strHrsToShow + ":" + datetime.get(Calendar.MINUTE) + " " + am_pm;
    }
}

