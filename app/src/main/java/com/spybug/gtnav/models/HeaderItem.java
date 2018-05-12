package com.spybug.gtnav.models;

import java.util.Date;

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

    public void setDate(Date date) {
        this.date = date;
    }
}
