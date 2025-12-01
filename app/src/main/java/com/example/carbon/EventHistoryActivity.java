package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;


/**
 * Activity that displays the history of past events the current user
 * has participated in or was associated with. Shows the user's status
 * for each past event and allows
 * navigation back to the main or profile screens.
 */
public class EventHistoryActivity extends AppCompatActivity {

    /**
     * RecyclerView displaying the list of past events
     */
    private RecyclerView recyclerView;

    /**
     * Adapter for the RecyclerView
     */
    private HistoryAdapter adapter;

    /**
     * List of past events for the user
     */
    private List<EventHistory> pastEvents = new ArrayList<>(); // <<< FIXED

    /**
     * TextView shown when there are no past events
     */
    private TextView emptyMessage;

    /**
     * Called when the activity is first created. Initializes the UI,
     * sets up the RecyclerView and adapter, loads past events from
     * Firestore, and configures bottom navigation buttons.
     *
     * @param savedInstanceState Bundle containing saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);

        recyclerView = findViewById(R.id.recycler_history);
        emptyMessage = findViewById(R.id.empty_message);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        adapter = new HistoryAdapter(pastEvents);

        recyclerView.setAdapter(adapter);

        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        loadPastEvents(currentUser.getUid());

        // bottom nav
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        findViewById(R.id.home_button).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.profile_button).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }


    /**
     * Loads all past events from Firestore and filters them to include
     * only events the user actually participated in, was selected for,
     * or appeared in the waitlist for. Only events that occurred before
     * the current date are included.
     *
     * @param uid The current user's UID
     */
    private void loadPastEvents(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").get()
                .addOnSuccessListener(querySnapshot -> {

                    pastEvents.clear(); // clear old data
                    Date now = new Date();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                        Event event = doc.toObject(Event.class);
                        if (event == null || event.getEventDate() == null)
                            continue;

                        // Only include past events
                        if (!event.getEventDate().before(now))
                            continue;

                        // Determine if user was actually part of this event
                        String userStatus = getUserStatus(doc, uid);

                        // Only add event to history if user ever signed up
                        if (!"NOT_IN_EVENT".equals(userStatus)) {
                            pastEvents.add(new EventHistory(event, userStatus));
                        }
                    }

                    // Update UI
                    adapter.notifyDataSetChanged();
                    emptyMessage.setVisibility(pastEvents.isEmpty() ? View.VISIBLE : View.GONE);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to load events: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // Helper method to get user status from document
    private String getUserStatus(DocumentSnapshot doc, String uid) {

        // Check selected entrants
        List<Map<String, Object>> selectedEntries =
                (List<Map<String, Object>>) doc.get("selectedEntrants");

        if (selectedEntries != null) {
            for (Map<String, Object> user : selectedEntries) {
                if (uid.equals(user.get("userId"))) {
                    return "Selected";
                }
            }
        }

        // Check waitlist entrants
        Map<String, Object> waitlistMap = (Map<String, Object>) doc.get("waitlist");

        if (waitlistMap != null) {
            List<Map<String, Object>> waitlistEntries =
                    (List<Map<String, Object>>) waitlistMap.get("waitlistEntrants");

            if (waitlistEntries != null) {
                for (Map<String, Object> user : waitlistEntries) {
                    if (uid.equals(user.get("userId"))) {
                        String status = (String) user.get("status");
                        return status != null ? status : "Not Selected";
                    }
                }
            }
        }

        // User never signed up for this event
        return "NOT_IN_EVENT";
    }

}
