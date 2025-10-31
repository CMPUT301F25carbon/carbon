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

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventVH> {

    private final List<Event> events = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Event> newList) {
        events.clear();
        events.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventVH holder, int position) {
        holder.bind(events.get(position), sdf, listener);
    }

    @Override public int getItemCount() { return events.size(); }

    static class EventVH extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvDesc, tvPoints;

        EventVH(@NonNull View itemView) {
            super(itemView);
            tvDate   = itemView.findViewById(R.id.tv_date);
            tvTitle  = itemView.findViewById(R.id.tv_title);
            tvDesc   = itemView.findViewById(R.id.tv_description);
            tvPoints = itemView.findViewById(R.id.tv_points);
        }

        void bind(Event e, SimpleDateFormat sdf, OnEventClickListener l) {
            tvDate.setText(sdf.format(e.getEventDate()));
            tvTitle.setText(e.getTitle());
            tvDesc.setText(e.getDescription());
            tvPoints.setText(e.getTotalSpots() + " spots");

            itemView.setOnClickListener(v -> l.onEventClick(e));
        }
    }
}