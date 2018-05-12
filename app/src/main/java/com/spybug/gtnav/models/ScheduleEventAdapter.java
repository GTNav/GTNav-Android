package com.spybug.gtnav.models;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spybug.gtnav.R;

import java.util.Collections;
import java.util.List;

public class ScheduleEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textHeader;

        HeaderViewHolder(View itemView) {
            super(itemView);
            textHeader = itemView.findViewById(R.id.header_item_text);
        }
    }

    private static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textEvent;

        EventViewHolder(View itemView) {
            super(itemView);
            textEvent = itemView.findViewById(R.id.event_item_text);
        }
    }

    @NonNull
    private List<ListItem> items = Collections.emptyList();

    public ScheduleEventAdapter(@NonNull List<ListItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ListItem.TYPE_HEADER: {
                View itemView = inflater.inflate(R.layout.header_list_item, parent, false);
                return new HeaderViewHolder(itemView);
            }
            case ListItem.TYPE_EVENT: {
                View itemView = inflater.inflate(R.layout.event_list_item, parent, false);
                return new EventViewHolder(itemView);
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ListItem.TYPE_HEADER: {
                HeaderItem header = (HeaderItem) items.get(position);
                HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
                // your logic here
                holder.textHeader.setText(header.getDate().toString());
                break;
            }
            case ListItem.TYPE_EVENT: {
                ScheduleEventItem event = (ScheduleEventItem) items.get(position);
                EventViewHolder holder = (EventViewHolder) viewHolder;
                // your logic here
                holder.textEvent.setText(event.toString());
                break;
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
