package com.spybug.gtnav.models;

public class ScheduleEventItem extends ListItem {

    private ScheduleEvent event;

    public ScheduleEventItem(ScheduleEvent event) {
        this.event = event;
    }

    @Override
    public int getType() {
        return TYPE_EVENT;
    }


    public ScheduleEvent getEvent() {
        return event;
    }

    public void setEvent(ScheduleEvent event) {
        this.event = event;
    }

    @Override
    public String toString() {
        if (event != null) {
            return event.toString();
        }
        else {
            return "ScheduleEventNull";
        }
    }
}
