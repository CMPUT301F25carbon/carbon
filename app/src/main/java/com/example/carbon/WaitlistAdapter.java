package com.example.carbon;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * An adapter to display a list of WaitlistEntrant objects in a RecyclerView.
 * It takes a list of entrants and binds their data to the views defined in
 * the item_waitlist_entrant.xml layout file.
 */
public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {

    private List<WaitlistEntrant> entrantList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);
    private OnSelectClickListener selectClickListener;

    public interface OnSelectClickListener {
        void onSelectClick(WaitlistEntrant entrant, int position);
    }

    public void setOnSelectClickListener(OnSelectClickListener listener) {
        this.selectClickListener = listener;
    }

    /**
     * Constructor for the adapter.
     * @param entrantList The initial list of waitlist entrants to display.
     */
    public WaitlistAdapter(List<WaitlistEntrant> entrantList) {
        this.entrantList = entrantList;
    }

    /**
     * Creates a new ViewHolder by inflating the layout for each item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single row from item_waitlist_entrant.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waitlist_entrant, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data from the WaitlistEntrant object at a given position to the ViewHolder's views.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model for this position
        WaitlistEntrant entrant = entrantList.get(position);
        // Set a placeholder while the user data is loading
        holder.userIdTextView.setText("Loading user...");

        // --- Use the new callback method ---
        String[] placeholderNames = {"John", "Luke", "Aahil"};
        int placeholderIndex = position % placeholderNames.length;


        entrant.fetchUserFromDB(new WaitlistEntrant.UserCallback() {
            @Override
            public void onUserFetched(User user) {

                // This code runs ONLY when the user is successfully fetched
                if (user != null && user.getFirstName() != null && user.getLastName() != null) {
                    holder.userIdTextView.setText("Name: " + user.getFirstName() + " " + user.getLastName());
                } else {
                    // Use placeholder if user data is incomplete
                    holder.userIdTextView.setText("Name: " + placeholderNames[placeholderIndex]);
                }
            }

            @Override
            public void onError(Exception e) {
                // Use placeholder instead of error message
                holder.userIdTextView.setText("Name: " + placeholderNames[placeholderIndex]);
                Log.e("WaitlistAdapter", "Failed to fetch user: " + entrant.getUserId(), e);
            }
        });

        // Format the registration date for display
        if (entrant.getRegistrationDate() != null) {
            holder.registrationDateTextView.setText("Registered on: " + dateFormat.format(entrant.getRegistrationDate()));
        } else {
            holder.registrationDateTextView.setText("Registration date not available");
        }

        // Add status
        String status = entrant.getStatus();
        holder.statusTextView.setText(status);

        if (Objects.equals(status, "Not Selected")) {
            holder.selectEntrantButton.setVisibility(View.VISIBLE);
            holder.selectEntrantButton.setOnClickListener(v -> {
                if (selectClickListener != null) {
                    selectClickListener.onSelectClick(entrant, position);
                }
            });
        } else {
            holder.selectEntrantButton.setVisibility(View.GONE);

            // show reason if status is Cancelled
            if ("Cancelled".equals(status)) {
                holder.reason.setVisibility(View.VISIBLE);

                String reason = entrant.getCancellationReason();

                holder.reason.setText(
                        (reason != null && !reason.isEmpty())
                                ? "Reason: " + reason
                                : "Reason not provided"
                );
            } else {
                holder.reason.setVisibility(View.GONE);
            }
        }

    }

    /**
     * Returns the total number of items in the list.
     */
    @Override
    public int getItemCount() {
        // Return 0 if the list is null to prevent crashes
        return entrantList == null ? 0 : entrantList.size();
    }

    /**
     * Updates the data set of the adapter and refreshes the RecyclerView.
     * @param newEntrantList The new list of entrants to display.
     */
    public void updateList(List<WaitlistEntrant> newEntrantList) {
        this.entrantList = newEntrantList;
        notifyDataSetChanged(); // Tells the RecyclerView to re-render with the new data
    }


    /**
     * ViewHolder class. Holds references to the views for a single item in the list,
     * which improves performance by avoiding repeated findViewById() calls.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userIdTextView;
        public TextView registrationDateTextView;
        public TextView statusTextView;
        public Button selectEntrantButton;

        public TextView reason;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views from the inflated layout
            userIdTextView = itemView.findViewById(R.id.tv_user_id);
            registrationDateTextView = itemView.findViewById(R.id.tv_registration_date);
            statusTextView = itemView.findViewById(R.id.tv_status);
            selectEntrantButton = itemView.findViewById(R.id.select_entrant_btn);
            reason = itemView.findViewById(R.id.tv_reason);
        }
    }
}
