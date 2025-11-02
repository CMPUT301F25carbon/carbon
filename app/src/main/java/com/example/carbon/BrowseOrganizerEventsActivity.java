package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class BrowseOrganizerEventsActivity extends AppCompatActivity {
    private EventList eventList;
    private EventsAdapter adapter; // Assuming you have a RecyclerView and an adapter
    private ArrayList<Event> displayedEvents = new ArrayList<>();
    private Button createEventButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_organizer_events);

        // Setup your RecyclerView and adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_events);
        adapter = new EventsAdapter(displayedEvents); // Initialize adapter with an empty list
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize your EventList
        eventList = new EventList();

        // Start fetching the data
        loadEventsFromDatabase();

        // Initialize The create new event button listener
        createEventButton = findViewById(R.id.create_event_btn);
        createEventButton.setOnClickListener(v -> {
            startActivity(new Intent(BrowseOrganizerEventsActivity.this, CreateEventActivity.class));
        });
    }

    private void loadEventsFromDatabase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // No user is logged in, do not proceed.
            Snackbar.make(findViewById(R.id.create_event_root), "You must be logged in to view your events.", Snackbar.LENGTH_LONG).show();
            // redirect to the login screen.
            startActivity(new Intent(BrowseOrganizerEventsActivity.this, LogInActivity.class));
            return;
        }
        String ownerId = currentUser.getUid(); // Get the user's unique ID

        eventList.fetchOrganizerEvents(ownerId, new EventList.EventListCallback() {
            @Override
            public void onEventsFetched(ArrayList<Event> events) {
                // Update your adapter's data and notify it to refresh the UI
                displayedEvents.clear();
                displayedEvents.addAll(events);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                // Handle the error (e.g., show a toast message)
                Toast.makeText(BrowseOrganizerEventsActivity.this, "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
