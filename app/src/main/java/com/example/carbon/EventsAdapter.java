package com.example.carbon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {

    private List<Event> events = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    public void updateList(List<Event> newList) {
        events.clear();
        events.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new VH(v);
    }



    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Event e = events.get(i);
        h.tvTitle.setText(e.getTitle());
        h.tvDate.setText(dateFormat.format(e.getEventDate()));
        h.tvLocation.setText(e.getEventLocation() + ", " + e.getEventCity());
        h.tvSpots.setText(e.getTotalSpots() + " spots");
    }

    @Override public int getItemCount() { return events.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvLocation, tvSpots;
        VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvDate = v.findViewById(R.id.tv_date);
            tvLocation = v.findViewById(R.id.tv_location);
            tvSpots = v.findViewById(R.id.tv_spots);
        }
    }
}