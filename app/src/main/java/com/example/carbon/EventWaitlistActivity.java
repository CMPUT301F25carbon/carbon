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

public class EventWaitlistActivity extends AppCompatActivity {
    private Waitlist waitlist;
    private WaitlistAdapter adapter;
    private ArrayList<WaitlistEntrant> displayedEntrants = new ArrayList<>();
    private Button createEventButton;

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
    }

    private void loadWaitlistFromDatabase(String eventId) {
        Log.d("Waitlist DB", eventId);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference the correct document in the 'events' collection using the eventId
        Query query = db.collection("events").whereEqualTo("uuid", eventId).limit(1);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 2. Check if the query returned any results
                if (!task.getResult().isEmpty()) {
                    // 3. Get the first (and only) document from the query result
                    DocumentSnapshot document = task.getResult().getDocuments().get(0);

                    // 4. Convert the document into an Event object
                    Event event = document.toObject(Event.class);

                    if (event != null && event.getWaitlist() != null) {
                        // 5. Get the nested Waitlist object from the Event
                        this.waitlist = event.getWaitlist();
                        List<WaitlistEntrant> entrants = this.waitlist.getWaitlistEntrants();

                        if (entrants != null) {
                            // 6. Update the adapter with the list of entrants
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
                        }
                    } else {
                        Toast.makeText(EventWaitlistActivity.this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
