package com.example.carbon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple adapter to display attendee names in the event details
 */
public class AttendeesAdapter extends RecyclerView.Adapter<AttendeesAdapter.ViewHolder> {
    private List<String> attendeeNames = new ArrayList<>();

    public void updateList(List<String> newList) {
        attendeeNames.clear();
        attendeeNames.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = attendeeNames.get(position);
        holder.tvName.setText(name);
    }

    @Override
    public int getItemCount() {
        return attendeeNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_attendee_name);
        }
    }
}

