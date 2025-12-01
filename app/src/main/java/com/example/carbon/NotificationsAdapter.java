package com.example.carbon;

/**
 * RecyclerView adapter for displaying notifications and basic metadata in admin views.
 * Outstanding issues: actions on items are delegated to parent; no swipe/mark interactions here.
 */

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

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    private List<Notification> notifications = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());

    public void updateList(List<Notification> newList) {
        notifications.clear();
        notifications.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Notification n = notifications.get(position);

        String title = n.getEventName();
        if (title == null || title.isEmpty()) title = "System Notification";
        holder.tvTitle.setText(title);

        String message = n.getMessage() != null ? n.getMessage() : "";
        if (n.getType() != null && !n.getType().equals("regular") && !n.getType().isEmpty()) {
            message = "[" + n.getType().toUpperCase(Locale.ROOT) + "] " + message;
        }
        holder.tvMessage.setText(message);

        if (n.getCreated_at() != null) {
            holder.tvMessage.append("\n" + dateFormat.format(n.getCreated_at()));
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvMessage;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle   = itemView.findViewById(R.id.notification_title);
            tvMessage = itemView.findViewById(R.id.notification_message);
        }
    }
}
