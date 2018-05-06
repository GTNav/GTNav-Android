package com.spybug.gtnav.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.spybug.gtnav.R;
import com.spybug.gtnav.models.ScheduleEvent;
import com.spybug.gtnav.utils.TextValidator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    private EditText nameEditText;
    private EditText locationEditText;
    private EditText timeEditText;
    private EditText startDateEditText;
    private EditText endDateEditText;
    private List<TextView> requiredTexts;

    private static final long MAX_WEEKS_BETWEEN = 24; //Max 24 week between start and end date

    private GregorianCalendar endDate;

    AddEventDialogListener mListener;

    public AddScheduleEventFragment() {
        // Required empty public constructor
        event = new ScheduleEvent(System.currentTimeMillis());
        repeatDayIndex = new boolean[7];
        requiredTexts = new ArrayList<>();
        endDate = null;
    }

    public interface AddEventDialogListener {
        void onDialogPositiveClick(List<ScheduleEvent> events);
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


        nameEditText = v.findViewById(R.id.event_name);
        nameEditText.addTextChangedListener(new TextValidator(nameEditText) {
            @Override
            public void validate(TextView textView, String text) {
                if (TextUtils.isEmpty(text)) {
                    textView.setError("Event name required");
                }
                else {
                    event.setEventName(text);
                }
                validateResponses();
            }
        });

        locationEditText = v.findViewById(R.id.location);
        locationEditText.addTextChangedListener(new TextValidator(locationEditText) {
            @Override
            public void validate(TextView textView, String text) {
                if (TextUtils.isEmpty(text)) {
                    textView.setError("Location required");
                }
                else {
                    event.setLocationName(text);
                }
                validateResponses();
            }
        });

        timeEditText = v.findViewById(R.id.time);
        timeEditText.setOnClickListener(timeEditClick);

        startDateEditText = v.findViewById(R.id.start_date);
        endDateEditText = v.findViewById(R.id.end_date);
        startDateEditText.setOnClickListener(dateEditClick);
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
                            validateResponses();

                            if (endDateEditText.getVisibility() == View.INVISIBLE) {
                                endDate = null;
                            }
                            else {
                                long dateDiffMs = endDate.getTimeInMillis() - event.getTime().getTimeInMillis();
                                long daysDiff = TimeUnit.MILLISECONDS.toDays(dateDiffMs);
                                if ((daysDiff / 7.0) > MAX_WEEKS_BETWEEN) {
                                    Toast.makeText(getContext(), "Time between dates is too large, please choose a closer end date", Toast.LENGTH_LONG).show();
                                    return; //Exit without saving
                                }
                            }

                            List<ScheduleEvent> events = createRepeatableEvents(event, endDate, repeatDayIndex);
                            listener.onDialogPositiveClick(events); //Send back events to listener

                        }
                        dialog.dismiss(); //Closes the dialog
                    }
                })
                .setNegativeButton(R.string.cancel_creation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss(); //do nothing when hit cancel, throw away changes
                    }
                });

        //Add texts to validate before submission
        requiredTexts.add(nameEditText);
        requiredTexts.add(timeEditText);
        requiredTexts.add(startDateEditText);
        requiredTexts.add(locationEditText);

        // Create the AlertDialog object and return it
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); //disabled by default
            }
        });
        return dialog;
    }

    //Used after user changes fields to allow saving of event if valid entries
    private void validateResponses() {

        boolean foundError = false;
        for (TextView text : requiredTexts) {
            if (text.getError() != null || !TextUtils.isEmpty(text.getError()) ||
                    TextUtils.isEmpty(text.getText().toString())) {
                foundError = true;
                break;
            }
        }

        if (endDateEditText.getVisibility() == View.VISIBLE &&
                TextUtils.isEmpty(endDateEditText.getText().toString())) {
            foundError = true;
        }

        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!foundError);
    }

    private List<ScheduleEvent> createRepeatableEvents(ScheduleEvent startEvent, GregorianCalendar endDate, boolean[] repeatDayIndex) {
        List<ScheduleEvent> repeatedEvents = new ArrayList<>();
        repeatedEvents.add(startEvent);

        List<Integer> repeatDaysOfWeekIndex = new ArrayList<>();
        for (int i = 0; i < repeatDayIndex.length; i++) {
            boolean val = repeatDayIndex[i];
            if (val) {
                repeatDaysOfWeekIndex.add(i + 1); // 1 = Sunday, 7 = Saturday
            }
        }

        if (repeatDaysOfWeekIndex.size() == 0 || endDate == null) {
            return repeatedEvents; // No repeat days so return one
        }

        GregorianCalendar startDate = startEvent.getTime(); //Start of repeated date for repeating days
        GregorianCalendar newStartOfWeekDate = ((GregorianCalendar) startDate.clone()); //First day of week for in between start/end date
        newStartOfWeekDate.set(Calendar.DAY_OF_WEEK, newStartOfWeekDate.getFirstDayOfWeek());

        //while the newStartOfWeekDate is before the endDate
        while (newStartOfWeekDate.compareTo(endDate) <= 0) {
            //Create date for every weekday that should be repeated
            for (Integer dayOfWeek : repeatDaysOfWeekIndex) {
                GregorianCalendar newDate = (GregorianCalendar) newStartOfWeekDate.clone();
                newDate.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                //newDate should always be greater then startDate because startDate already added and don't want days before start
                if (newDate.compareTo(startDate) > 0) {
                    if (newDate.compareTo(endDate) <= 0) {
                        ScheduleEvent newEvent = (ScheduleEvent) startEvent.clone();
                        newEvent.setTime(newDate);
                        repeatedEvents.add(newEvent);
                    }
                    //Greater then end date so stop looping
                    else {
                        return repeatedEvents;
                    }
                }
            }
            newStartOfWeekDate.add(Calendar.DATE, 7); //Increment by 7 days/1 week
        }

        return repeatedEvents;
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
                        validateResponses();
                    }
                    allUnchecked = false;
                }
            }

            if (allUnchecked && end_date.getVisibility() == View.VISIBLE) {
                end_date.setVisibility(View.INVISIBLE);
                validateResponses();
            }
        }
    };

    private View.OnClickListener timeEditClick = new EditText.OnClickListener() {
        @Override
        public void onClick(View view) {
            int hour;
            int minute;

            final EditText timeEdit = (EditText) view;

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
                    timestamp.set(Calendar.SECOND, 0);
                    timestamp.set(Calendar.MILLISECOND, 0);
                    event.setTime(timestamp);

                    String timeString = timeTo12HR(hourOfDay, minute);
                    timeEdit.setText(timeString);
                    validateResponses();
                }
            }, hour, minute, false);
            timePicker.setTitle("Select time");
            timePicker.show();
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

                    //Save start date to ScheduleEvent object
                    if (view.getId() == R.id.start_date) {
                        GregorianCalendar savedTime = event.getTime();
                        savedTime.set(year, month, day);
                        event.setTime(savedTime);
                    }
                    //Save end date to variable for use later
                    else {
                        if (endDate == null) {
                            endDate = (GregorianCalendar) GregorianCalendar.getInstance();
                        }
                        endDate.set(year, month, day, 0, 0);
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
                    String dateString = dateFormat.format(dateResult.getTime());
                    ((EditText) view).setText(dateString);
                    validateResponses();
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

