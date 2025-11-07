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
        entrant.fetchUserFromDB(new WaitlistEntrant.UserCallback() {
            @Override
            public void onUserFetched(User user) {
                // This code runs ONLY when the user is successfully fetched
                if (user != null) {
                    holder.userIdTextView.setText("Name: " + user.getFirstName() + " " + user.getLastName());
                } else {
                    holder.userIdTextView.setText("User not found");
                }
            }

            @Override
            public void onError(Exception e) {
                // Handle the error if the user fetch fails
                holder.userIdTextView.setText("Error loading user");
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
        if (!Objects.equals(entrant.getStatus(), "Not Selected")) { // If is any status other than selected
            holder.statusTextView.setText(entrant.getStatus());
            holder.selectEntrantButton.setVisibility(View.GONE);
        } else {
            holder.statusTextView.setText(entrant.getStatus());
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the views from the inflated layout
            userIdTextView = itemView.findViewById(R.id.tv_user_id);
            registrationDateTextView = itemView.findViewById(R.id.tv_registration_date);
            statusTextView = itemView.findViewById(R.id.tv_status);
            selectEntrantButton = itemView.findViewById(R.id.select_entrant_btn);
        }
    }
}
