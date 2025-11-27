package com.example.carbon;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE";
    public static final String EXTRA_EVENT_DATE = "EXTRA_EVENT_DATE";     // e.g., "05/12/2025"
    public static final String EXTRA_EVENT_COUNTS = "EXTRA_EVENT_COUNTS"; // e.g., "11 registrations / 5 spots"

    private TextView tvTitle, tvDate, tvCounts, tvDescription, tvCountdown;
    private RecyclerView rvRegistrants;
    private AttendeesAdapter attendeesAdapter;

    private String eventId;
    private Event currentEvent;
    private Handler countdownHandler;
    private Runnable countdownRunnable;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        UIHelper.setupHeaderAndMenu(this);

        tvTitle  = findViewById(R.id.tv_event_title);
        tvDate   = findViewById(R.id.tv_event_date);
        tvCounts = findViewById(R.id.tv_event_counts);
        tvDescription = findViewById(R.id.tv_event_description);
        tvCountdown = findViewById(R.id.tv_countdown);
        rvRegistrants = findViewById(R.id.rv_registrants);
        
        countdownHandler = new Handler(Looper.getMainLooper());

        Intent intent = getIntent();
        String eventUuid = null;

        // Check if the activity was launched by a deep link
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                // The UUID is the last part of the path in "carbondate://events/UUID"
                eventUuid = uri.getLastPathSegment();
            }
        } else {
            // Fallback to the old way (launched from another activity)
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
                    EventDetailsActivity.this.eventId = event.getUuid();
                    
                    tvTitle.setText(event.getTitle());
                    if (event.getEventDate() != null) {
                        // Format date and time
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("h a", Locale.US); // e.g., "9 AM", "2 PM"
                        String dateStr = dateFormat.format(event.getEventDate());
                        String timeStr = timeFormat.format(event.getEventDate()).toLowerCase();
                        tvDate.setText(dateStr + " at " + timeStr);
                        
                        // Start countdown timer
                        startCountdown(event.getEventDate());
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

    /**
     * Starts the countdown timer for the event
     * @param eventDate The date/time of the event
     */
    private void startCountdown(Date eventDate) {
        if (eventDate == null) {
            tvCountdown.setText("Event date not available");
            return;
        }

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long eventTime = eventDate.getTime();
                long diff = eventTime - now;

                if (diff <= 0) {
                    tvCountdown.setText("Event has started!");
                    return;
                }

                long days = TimeUnit.MILLISECONDS.toDays(diff);
                long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;

                String countdownText;
                if (days > 0) {
                    countdownText = String.format(Locale.US, "%d days, %d hours", days, hours);
                } else if (hours > 0) {
                    countdownText = String.format(Locale.US, "%d hours, %d minutes", hours, minutes);
                } else if (minutes > 0) {
                    countdownText = String.format(Locale.US, "%d minutes, %d seconds", minutes, seconds);
                } else {
                    countdownText = String.format(Locale.US, "%d seconds", seconds);
                }

                tvCountdown.setText(countdownText);
                countdownHandler.postDelayed(this, 1000); // Update every second
            }
        };

        countdownHandler.post(countdownRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }
}

