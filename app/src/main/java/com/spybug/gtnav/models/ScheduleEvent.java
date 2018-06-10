package com.spybug.gtnav.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.spybug.gtnav.utils.CalendarConverter;
import com.spybug.gtnav.utils.LatLngConverter;

import java.util.GregorianCalendar;

@Entity(tableName = "schedule_event")
public class ScheduleEvent extends ListItem implements Cloneable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "group_id")
    private long groupId; //Unique groupId for same reoccurring events

    @ColumnInfo(name = "loc_name")
    private String locationName;

    @ColumnInfo(name = "event_name")
    private String eventName;

    @ColumnInfo(name = "loc")
    @TypeConverters({LatLngConverter.class})
    private LatLng location;

    @ColumnInfo(name = "time")
    @TypeConverters({CalendarConverter.class})
    private GregorianCalendar time;

    public ScheduleEvent() {}

    @Ignore
    public ScheduleEvent(long groupId) {
        this.groupId = groupId; //Empty new schedule event
        this.time = (GregorianCalendar) GregorianCalendar.getInstance();
        this.location = new LatLng();
        this.eventName = "";
        this.locationName = "";
    }

    public ScheduleEvent(String eventName, long groupId, String locName, LatLng loc, GregorianCalendar time) {
        this.eventName = eventName;
        this.locationName = locName;
        this.time = time;
        this.location = loc;
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "event: " + eventName +
                "\nloc: " + locationName +
                "\ngroup: " + groupId +
                "\ncoord: " + location.getLatitude() + "," + location.getLongitude() +
                "\ntime: " + time.getTime().toString();
    }

    public Object clone() {
        try {
            ScheduleEvent cloned = (ScheduleEvent) super.clone();
            cloned.setTime((GregorianCalendar) cloned.getTime().clone());
            return cloned;
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getEventName() {
        return eventName;
    }

    public GregorianCalendar getTime() {
        return time;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setTime(GregorianCalendar time) {
        this.time = time;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getType() {
        return TYPE_EVENT;
    }
}
