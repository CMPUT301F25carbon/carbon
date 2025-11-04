package com.example.carbon;

import android.content.Intent;
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

    public interface OnDeleteClickListener {
        void onDelete(Event event, int position);
    }

    public interface OnLongPressListener {
        void onLongPress();
    }

    public void setDeleteListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
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

        // delete button only in edit mode
        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        if (isEditMode) {
            h.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(e, h.getAdapterPosition());
                }
            });
            // disable open-details click when editing
            h.itemView.setOnClickListener(null);
        } else {
            h.btnDelete.setOnClickListener(null);

            // ✅ Open Event Details page on tap (UML: Home → Event Details)
            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);

                // Pass what we safely have; EventDetailsActivity handles missing fields gracefully
                intent.putExtra(EventDetailsActivity.EXTRA_EVENT_TITLE, e.getTitle());
                intent.putExtra(EventDetailsActivity.EXTRA_EVENT_DATE, dateFormat.format(e.getEventDate()));
                intent.putExtra(
                        EventDetailsActivity.EXTRA_EVENT_COUNTS,
                        e.getTotalSpots() + " spots"
                );
                // If your Event has an ID method later, you can add:
                // intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, e.getId());

                v.getContext().startActivity(intent);
            });
        }

        // Long press toggles edit mode (unchanged)
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
