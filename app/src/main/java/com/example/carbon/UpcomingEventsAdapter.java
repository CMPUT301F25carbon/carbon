package com.example.carbon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


/**
 * RecyclerView Adapter to display a list of upcoming events.
 * Each item shows the event's title and date. Designed for use
 * in the ProfileActivity or any other screen that shows upcoming events.
 */
public class UpcomingEventsAdapter extends RecyclerView.Adapter<UpcomingEventsAdapter.EventViewHolder> {

    private List<Event> events;
    private FirebaseUser currentUser;

    public UpcomingEventsAdapter(List<Event> events, FirebaseUser currentUser) {
        this.events = events;
        this.currentUser = currentUser;
    }

    public void submitList(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.title.setText(event.getTitle());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        holder.date.setText(event.getEventDate() != null ?
                dateFormat.format(event.getEventDate()) :
                "Date not available");

        //No status needed for upcoming events
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            date = itemView.findViewById(R.id.tv_date);
        }
    }
}
