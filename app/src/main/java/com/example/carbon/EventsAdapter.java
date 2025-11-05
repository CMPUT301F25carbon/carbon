package com.example.carbon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private boolean isEditMode = false;
    private OnDeleteClickListener deleteListener;
    private OnLongPressListener longPressListener;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public EventsAdapter(ArrayList<Event> displayedEvents) {
        this.events = displayedEvents;
    }

    public interface OnDeleteClickListener {
        void onDelete(Event event, int position);
    }

    public interface OnLongPressListener {
        void onLongPress();
    }

    public void setDeleteListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setLongPressListener(OnLongPressListener listener) {
        this.longPressListener = listener;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

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

        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        h.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(e);
            }
        });

        if (isEditMode) {
            h.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(e, h.getAdapterPosition());
                }
            });
        } else {
            h.btnDelete.setOnClickListener(null);
        }

        h.itemView.setOnLongClickListener(v -> {
            if (longPressListener != null) {
                longPressListener.onLongPress();
            }
            return true;
        });
    }

    @Override public int getItemCount() { return events.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvLocation, tvSpots;
        ImageButton btnDelete;
        VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvDate = v.findViewById(R.id.tv_date);
            tvLocation = v.findViewById(R.id.tv_location);
            tvSpots = v.findViewById(R.id.tv_spots);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
