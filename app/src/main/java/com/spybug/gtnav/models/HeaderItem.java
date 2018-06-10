package com.spybug.gtnav.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HeaderItem extends ListItem {
    private Date date;

    public HeaderItem(Date date) {
        this.date = date;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    public Date getDate() {
        return date;
    }

    public String getDateString() {
        return new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
                .format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
