package com.example.carbon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<EventHistory> list;

    public HistoryAdapter(List<EventHistory> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        EventHistory item = list.get(pos);

        holder.name.setText(item.getEvent().getTitle());
        holder.status.setText(item.getStatus());

        // Set status color
        switch (item.getStatus()) {
            case "Selected":  holder.status.setTextColor(0xFF388E3C); break; // green
            case "Cancelled": holder.status.setTextColor(0xFFD32F2F); break; // red
            default:          holder.status.setTextColor(0xFF757575);        // gray
        }

        // Display only the date
        Date eventDate = item.getEvent().getEventDate();
        if (eventDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getDefault()); // use device timezone
            holder.date.setText(sdf.format(eventDate));
        } else {
            holder.date.setText("No date");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, status, date;

        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.historyEventName);
            status = v.findViewById(R.id.historyStatus);
            date = v.findViewById(R.id.historyEventDate);
        }
    }
}
