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
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.spybug.gtnav.R;
import com.spybug.gtnav.models.ScheduleEvent;

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
    private boolean[] repeatDayIndex; //0 = Sun, 1 = Mon, etc...
    private EditText endDateEditText;

    AddEventDialogListener mListener;

    public AddScheduleEventFragment() {
        // Required empty public constructor
        event = new ScheduleEvent(System.currentTimeMillis());
        repeatDayIndex = new boolean[7];
    }

    public interface AddEventDialogListener {
        void onDialogPositiveClick(ScheduleEvent event);
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


        final EditText nameEdit = v.findViewById(R.id.name);
        nameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    EditText nameEdit = (EditText) view;
                    event.setEventName(nameEdit.getText().toString());
                }
            }
        });

        final EditText locationEdit = v.findViewById(R.id.location);
        locationEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    EditText locationEdit = (EditText) view;
                    event.setLocationName(locationEdit.getText().toString());
                }
            }
        });

        final EditText timeEdit = v.findViewById(R.id.time);
        timeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour;
                int minute;

                //Get time from stored ScheduleEvent value if there, otherwise use current
                GregorianCalendar timestamp = event.getTime();
                if (timestamp == null) {
                    GregorianCalendar currentTime = (GregorianCalendar) GregorianCalendar.getInstance();
                    hour = currentTime.get(Calendar.HOUR_OF_DAY);
                    minute = currentTime.get(Calendar.MINUTE);
                    event.setTime(currentTime);
                }
                else {
                    hour =  timestamp.get(Calendar.HOUR_OF_DAY);
                    minute = timestamp.get(Calendar.MINUTE);
                }

                TimePickerDialog timePicker;
                timePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        GregorianCalendar timestamp = event.getTime();
                        if (timestamp == null) {
                            timestamp = (GregorianCalendar) GregorianCalendar.getInstance();
                        }
                        timestamp.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        timestamp.set(Calendar.MINUTE, minute);
                        event.setTime(timestamp);

                        String timeString = timeTo12HR(hourOfDay, minute);
                        timeEdit.setText(timeString);
                    }
                }, hour, minute, false);
                timePicker.setTitle("Select time");
                timePicker.show();
            }
        });

        EditText startDateEdit = v.findViewById(R.id.start_date);
        endDateEditText = v.findViewById(R.id.end_date);
        startDateEdit.setOnClickListener(dateEditClick);
        endDateEditText.setOnClickListener(dateEditClick);

        LinearLayout checkboxLinearLayout = v.findViewById(R.id.checkboxes_linearlayout);
        for (int i = 0; i < checkboxLinearLayout.getChildCount(); i++) {
            View child = checkboxLinearLayout.getChildAt(i);
            child.setOnClickListener(checkBoxClick);
        }

        builder.setView(v)
                .setCancelable(true)
                .setPositiveButton(R.string.submit_creation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddEventDialogListener listener = (AddEventDialogListener) getTargetFragment();
                        if (listener != null) {
                            validateResponses(v);

                            listener.onDialogPositiveClick(event);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel_creation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss(); //do nothing when hit cancel, throw away changes
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private boolean validateResponses(View v) {
        EditText locationEdit = v.findViewById(R.id.location);
        EditText nameEdit = v.findViewById(R.id.name);

        String locationEditText = locationEdit.getText().toString();
        event.setLocationName(locationEditText);

        String eventNameText = nameEdit.getText().toString();
        event.setEventName(eventNameText);

        return true;
    }

    private CheckedTextView.OnClickListener checkBoxClick = new CheckedTextView.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckedTextView checkbox = (CheckedTextView) view;
            checkbox.toggle();

            int index;
            switch (view.getId()) {
                case R.id.sunday_checkbox:
                    index = 0;
                    break;
                case R.id.monday_checkbox:
                    index = 1;
                    break;
                case R.id.tuesday_checkbox:
                    index = 2;
                    break;
                case R.id.wednesday_checkbox:
                    index = 3;
                    break;
                case R.id.thursday_checkbox:
                    index = 4;
                    break;
                case R.id.friday_checkbox:
                    index = 5;
                    break;
                case R.id.saturday_checkbox:
                    index = 6;
                    break;
                default:
                    return;
            }

            repeatDayIndex[index] = checkbox.isChecked();

            EditText end_date = endDateEditText;
            boolean allUnchecked = true;
            for (boolean checked : repeatDayIndex) {
                if (checked) {
                    if (end_date.getVisibility() == View.INVISIBLE) {
                        end_date.setVisibility(View.VISIBLE);
                    }
                    allUnchecked = false;
                }
            }

            if (allUnchecked && end_date.getVisibility() == View.VISIBLE) {
                end_date.setVisibility(View.INVISIBLE);
            }

        }
    };

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
        String strMinsToShow = (datetime.get(Calendar.MINUTE) < 10) ? "0" + datetime.get(Calendar.MINUTE) : datetime.get(Calendar.MINUTE)+"";
        return strHrsToShow + ":" + strMinsToShow + " " + am_pm;
    }
}

