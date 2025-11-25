package com.example.carbon;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE";
    public static final String EXTRA_EVENT_DATE = "EXTRA_EVENT_DATE";     // e.g., "05/12/2025"
    public static final String EXTRA_EVENT_COUNTS = "EXTRA_EVENT_COUNTS"; // e.g., "11 registrations / 5 spots"

    private TextView tvTitle, tvDate, tvCounts, tvDescription;
    private RecyclerView rvRegistrants;
    private AttendeesAdapter attendeesAdapter;

    private String eventId;
    private Event currentEvent;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        UIHelper.setupHeaderAndMenu(this);

        tvTitle  = findViewById(R.id.tv_event_title);
        tvDate   = findViewById(R.id.tv_event_date);
        tvCounts = findViewById(R.id.tv_event_counts);
        tvDescription = findViewById(R.id.tv_event_description);
        rvRegistrants = findViewById(R.id.rv_registrants);

        Intent intent = getIntent();
        String eventUuid = null;

        // ✅ Check if the activity was launched by a deep link
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                // The UUID is the last part of the path in "carbondate://events/UUID"
                eventUuid = uri.getLastPathSegment();
            }
        } else {
            // ✅ Fallback to the old way (launched from another activity)
            eventUuid = intent.getStringExtra("EXTRA_EVENT_ID");
        }

        // Now, use the eventUuid to load your data
        if (eventUuid != null && !eventUuid.isEmpty()) {
            // Your existing logic to load event details from Firestore using the UUID
            loadEventDataFromFirestore(eventUuid);
        } else {
            // Handle the error: no ID was found
            Toast.makeText(this, "Event ID not found.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if there's no data to show
        }
        String title  = getIntent().getStringExtra(EXTRA_EVENT_TITLE);
        String date   = getIntent().getStringExtra(EXTRA_EVENT_DATE);
        String counts = getIntent().getStringExtra(EXTRA_EVENT_COUNTS);

        tvTitle.setText(title != null ? title : "Event");
        tvDate.setText(date != null ? date : "");
        tvCounts.setText(counts != null ? counts : "");

        // Setup attendees RecyclerView
        rvRegistrants.setLayoutManager(new LinearLayoutManager(this));
        attendeesAdapter = new AttendeesAdapter();
        rvRegistrants.setAdapter(attendeesAdapter);
    }

    /**
     * Loads the event based on the passed ID and updates the titles and strings for user visualization
     * @param eventId The ID of the event to view the waitlist of
     *
     * @author Cooper Goddard
     */
    private void loadEventDataFromFirestore(String eventId) {
        Log.d("Event DB", eventId);
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
                    currentEvent = event;
                    eventId = event.getUuid();
                    
                    tvTitle.setText(event.getTitle());
                    if (event.getEventDate() != null) {
                        tvDate.setText(event.getEventDate().toString());
                    }
                    
                    // Display description
                    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                        tvDescription.setText(event.getDescription());
                    } else {
                        tvDescription.setText("No description available.");
                    }
                    
                    // Load and display attendees
                    loadAttendees(event);

                } else {
                    Toast.makeText(EventDetailsActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(EventDetailsActivity.this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Loads attendees from the event and displays them
     * @param event The event object containing attendee list
     */
    private void loadAttendees(Event event) {
        if (event == null || event.getAttendeeList() == null || event.getAttendeeList().isEmpty()) {
            attendeesAdapter.updateList(new ArrayList<>());
            return;
        }

        List<String> attendeeIds = event.getAttendeeList();
        String[] placeholderNames = {"John", "Luke", "Aahil"};
        int[] placeholderIndex = {0};
        
        // Initialize list with placeholders (will be replaced if user found)
        List<String> attendeeNames = new ArrayList<>();
        for (int i = 0; i < attendeeIds.size(); i++) {
            attendeeNames.add(placeholderNames[placeholderIndex[0] % placeholderNames.length]);
            placeholderIndex[0]++;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int[] loadedCount = {0};
        int totalCount = attendeeIds.size();

        for (int i = 0; i < attendeeIds.size(); i++) {
            final int index = i;
            String userId = attendeeIds.get(i);
            
            if (userId == null || userId.isEmpty()) {
                // Keep placeholder for invalid userId
                loadedCount[0]++;
                if (loadedCount[0] == totalCount) {
                    attendeesAdapter.updateList(attendeeNames);
                }
                continue;
            }

            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            User user = task.getResult().toObject(User.class);
                            if (user != null && user.getFirstName() != null && user.getLastName() != null) {
                                attendeeNames.set(index, user.getFirstName() + " " + user.getLastName());
                            }
                            // Otherwise keep placeholder
                        }
                        // If user not found, keep placeholder
                        
                        loadedCount[0]++;
                        if (loadedCount[0] == totalCount) {
                            attendeesAdapter.updateList(attendeeNames);
                        }
                    });
        }
    }
}

