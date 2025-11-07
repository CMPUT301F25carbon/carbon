package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The EventWaitlistActivity holds the logic of the activity_event_waitlist.xml page.
 * Allows an organizer to view the waitlist of the event that they own.
 * It is expected that the activity is passed an EVENT_ID through the activity intent
 *
 * @author Cooper Goddard
 */
public class EventWaitlistActivity extends AppCompatActivity {
    private Waitlist waitlist;
    private WaitlistAdapter adapter;
    private ArrayList<WaitlistEntrant> displayedEntrants = new ArrayList<>();
    private Button viewInvitedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_waitlist);
        // Setup header and footer
        UIHelper.setupHeaderAndMenu(this);
        // Validate Login
        Intent intent = getIntent();
        String eventId = intent.getStringExtra("EVENT_ID");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID not found.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if there's no ID
            return;
        }
        // Setup your RecyclerView and adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_waitlist);
        adapter = new WaitlistAdapter(displayedEntrants); // Initialize adapter with an empty list
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize your Waitlist
        waitlist = new Waitlist();

        // Start fetching the data
        loadWaitlistFromDatabase(eventId);

        // Set up "View Invited" button listener
        viewInvitedButton = findViewById(R.id.view_invited_btn);
        viewInvitedButton.setOnClickListener(v -> {
            // Create an Intent to start SelectedListAcvitvity
            Intent newIntent = new Intent(EventWaitlistActivity.this, SelectedListActivity.class);

            // Pass the unique ID of the clicked event to the next activity.
            newIntent.putExtra("EVENT_ID", eventId);

            // Start the new activity
            startActivity(newIntent);
        });
    }

    /**
     * Loads the waitlist of the event based on the passed ID, loads all waitlist entrants into adapter for visualization
     * @param eventId The ID of the event to view the waitlist of
     *
     * @author Cooper Goddard
     */
    private void loadWaitlistFromDatabase(String eventId) {
        Log.d("Waitlist DB", eventId);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference the correct document in the 'events' collection using the eventId
        Query query = db.collection("events").whereEqualTo("uuid", eventId).limit(1);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Check if the query returned any results
                if (!task.getResult().isEmpty()) {
                    // Get the first (and only, otherwise something is very wrong lol) document from the query result
                    DocumentSnapshot document = task.getResult().getDocuments().get(0);

                    // Convert the document into an Event object
                    Event event = document.toObject(Event.class);

                    if (event != null && event.getWaitlist() != null) {
                        // Get the nested Waitlist object from the Event
                        this.waitlist = event.getWaitlist();
                        List<WaitlistEntrant> entrants = this.waitlist.getWaitlistEntrants();

                        if (entrants != null) {
                            // Update the adapter with the list of entrants
                            displayedEntrants.clear();
                            displayedEntrants.addAll(entrants);
                            adapter.notifyDataSetChanged();
                            Log.d("Waitlist DB", "Successfully loaded " + entrants.size() + " entrants.");
                        }

                            } else {
                                Toast.makeText(EventWaitlistActivity.this, "Waitlist data is missing in this event.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EventWaitlistActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(EventWaitlistActivity.this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}
