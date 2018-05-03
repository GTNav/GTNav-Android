package com.spybug.gtnav.utils;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarConverter {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US); //ISO8601 format

    @TypeConverter
    public static String toString(GregorianCalendar calendar) {
        dateFormat.setTimeZone(calendar.getTimeZone());
        dateFormat.setCalendar(calendar);
        return dateFormat.format(calendar.getTime());
    }

    @TypeConverter
    public static GregorianCalendar toCalendar(String isoString) {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance(TimeZone.getDefault(), Locale.US);
        try {
            Date date = dateFormat.parse(isoString);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

}
