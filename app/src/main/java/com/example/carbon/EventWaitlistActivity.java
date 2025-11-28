package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private Button redrawButton;
    private Button notifyAllButton;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_waitlist);
        // Setup header and footer
        UIHelper.setupHeaderAndMenu(this);
        // Validate Login
        Intent intent = getIntent();
        eventId = intent.getStringExtra("EVENT_ID");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID not found.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if there's no ID
            return;
        }
        // Setup your RecyclerView and adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_waitlist);
        adapter = new WaitlistAdapter(displayedEntrants); // Initialize adapter with an empty list
        adapter.setOnSelectClickListener(this::handleSelectEntrant);
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

        Button viewAcceptedButton = findViewById(R.id.view_accepted_btn);
        viewAcceptedButton.setOnClickListener(v -> {
            Intent newIntent = new Intent(EventWaitlistActivity.this, AcceptedListActivity.class);
            newIntent.putExtra("EVENT_ID", eventId);
            startActivity(newIntent);
        });


        // Set up "Notify All Waitlist" button listener
        notifyAllButton = findViewById(R.id.notify_all_btn);
        notifyAllButton.setOnClickListener(v -> showBroadcastDialog());

        redrawButton = findViewById(R.id.redraw_btn);
        redrawButton.setOnClickListener(v -> redrawPendingEntrants());
    }

    /**
     * Shows a dialog for the organizer to enter a message to broadcast to all waitlist entrants
     */
    private void showBroadcastDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_broadcast_message, null);
        EditText messageEditText = dialogView.findViewById(R.id.et_broadcast_message);

        new AlertDialog.Builder(this)
                .setTitle("Notify All Waitlist")
                .setView(dialogView)
                .setPositiveButton("Send", (dialog, which) -> {
                    String message = messageEditText.getText().toString().trim();
                    if (message.isEmpty()) {
                        Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                    } else {
                        sendBroadcastNotification(message);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Sends a broadcast notification to all waitlist entrants
     * @param message the message to send
     */
    private void sendBroadcastNotification(String message) {
        FirebaseNotificationService notificationService = new FirebaseNotificationService();
        
        notificationService.broadcastNotificationToWaitlist(
                eventId,
                message,
                () -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Notification sent to all waitlist entrants", Toast.LENGTH_SHORT).show();
                    });
                },
                e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Failed to send notifications: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("EventWaitlistActivity", "Failed to broadcast notification", e);
                    });
                }
        );
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

    /**
     * Handles when organizer clicks "Select" button on an entrant
     * Shows prompt to remove or replace with random selection
     */
    private void handleSelectEntrant(WaitlistEntrant entrant, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Select Entrant")
                .setMessage("What would you like to do with this entrant?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removeEntrant(entrant);
                })
                .setNeutralButton("Replace (Random)", (dialog, which) -> {
                    replaceEntrantWithRandom(entrant);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Removes the entrant from the waitlist
     */
    private void removeEntrant(WaitlistEntrant entrant) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("events").whereEqualTo("uuid", eventId).limit(1);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                String eventDocId = task.getResult().getDocuments().get(0).getId();
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                Event event = document.toObject(Event.class);

                if (event != null && event.getWaitlist() != null) {
                    List<WaitlistEntrant> entrants = event.getWaitlist().getWaitlistEntrants();
                    if (entrants != null) {
                        entrants.remove(entrant);
                        db.collection("events").document(eventDocId)
                                .update("waitlist.waitlistEntrants", entrants)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Entrant removed", Toast.LENGTH_SHORT).show();
                                    loadWaitlistFromDatabase(eventId);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to remove entrant", Toast.LENGTH_SHORT).show();
                                    Log.e("EventWaitlistActivity", "Failed to remove entrant", e);
                                });
                    }
                }
            }
        });
    }

    /**
     * Replaces the entrant with a random selection from available entrants
     */
    private void replaceEntrantWithRandom(WaitlistEntrant entrantToReplace) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("events").whereEqualTo("uuid", eventId).limit(1);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                String eventDocId = task.getResult().getDocuments().get(0).getId();
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                Event event = document.toObject(Event.class);

                if (event != null && event.getWaitlist() != null) {
                    List<WaitlistEntrant> allEntrants = event.getWaitlist().getWaitlistEntrants();
                    
                    // Find available entrants (status = "Not Selected" and not the one being replaced)
                    List<WaitlistEntrant> availableEntrants = new ArrayList<>();
                    for (WaitlistEntrant e : allEntrants) {
                        if (e != null && Objects.equals(e.getStatus(), "Not Selected") && 
                            !e.getUserId().equals(entrantToReplace.getUserId())) {
                            availableEntrants.add(e);
                        }
                    }

                    if (availableEntrants.isEmpty()) {
                        Toast.makeText(this, "No available entrants to replace with", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Randomly select one
                    java.util.Collections.shuffle(availableEntrants);
                    WaitlistEntrant replacement = availableEntrants.get(0);
                    
                    // Update: remove old, set replacement to "Pending"
                    allEntrants.remove(entrantToReplace);
                    replacement.setStatus("Pending");
                    replacement.setSelectionDate(new Date());

                    db.collection("events").document(eventDocId)
                            .update("waitlist.waitlistEntrants", allEntrants)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Entrant replaced with random selection", Toast.LENGTH_SHORT).show();
                                loadWaitlistFromDatabase(eventId);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to replace entrant", Toast.LENGTH_SHORT).show();
                                Log.e("EventWaitlistActivity", "Failed to replace entrant", e);
                            });
                }
            }
        });
    }

    /**
     * Re-draws the lottery:
     * - All Pending entrants are marked as "No Response"
     * - For each Pending entrant (as long as there are Not Selected entrants),
     *   a random Not Selected entrant is set to "Pending" with a new selectionDate.
     */
    private void redrawPendingEntrants() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("events").whereEqualTo("uuid", eventId).limit(1);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult().isEmpty()) {
                Toast.makeText(this, "Failed to load event for redraw.", Toast.LENGTH_SHORT).show();
                return;
            }

            String eventDocId = task.getResult().getDocuments().get(0).getId();
            DocumentSnapshot document = task.getResult().getDocuments().get(0);
            Event event = document.toObject(Event.class);

            if (event == null || event.getWaitlist() == null) {
                Toast.makeText(this, "Waitlist data is missing for this event.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<WaitlistEntrant> entrants = event.getWaitlist().getWaitlistEntrants();
            if (entrants == null || entrants.isEmpty()) {
                Toast.makeText(this, "No entrants found in the waitlist.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Collect all Pending and all Not Selected entrants
            List<WaitlistEntrant> pendingEntrants = new ArrayList<>();
            List<WaitlistEntrant> notSelectedEntrants = new ArrayList<>();

            for (WaitlistEntrant e : entrants) {
                if (e == null || e.getStatus() == null) continue;
                switch (e.getStatus()) {
                    case "Pending":
                        pendingEntrants.add(e);
                        break;
                    case "Not Selected":
                        notSelectedEntrants.add(e);
                        break;
                }
            }

            if (pendingEntrants.isEmpty()) {
                Toast.makeText(this, "There are no Pending entrants to redraw.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (notSelectedEntrants.isEmpty()) {
                // We can still mark all Pending entrants as No Response, but cannot select replacements
                for (WaitlistEntrant p : pendingEntrants) {
                    p.setStatus("No Response");
                    createInvitationRevokedNotification(p, event);
                }

                db.collection("events").document(eventDocId)
                        .update("waitlist.waitlistEntrants", entrants)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "All Pending entrants marked as No Response (no replacements available).", Toast.LENGTH_SHORT).show();
                            loadWaitlistFromDatabase(eventId);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update entrants during redraw.", Toast.LENGTH_SHORT).show();
                            Log.e("EventWaitlistActivity", "Redraw update failed", e);
                        });
                return;
            }

            // Randomize the order of Not Selected entrants
            Collections.shuffle(notSelectedEntrants);

            // Assign replacements for as many Pending entrants as possible
            int pairs = Math.min(pendingEntrants.size(), notSelectedEntrants.size());
            Date now = new Date();

            for (int i = 0; i < pairs; i++) {
                WaitlistEntrant pending = pendingEntrants.get(i);
                WaitlistEntrant replacement = notSelectedEntrants.get(i);

                // Old Pending entrant → No Response
                pending.setStatus("No Response");
                createInvitationRevokedNotification(pending, event);

                // Random Not Selected entrant → Pending with new selectionDate
                replacement.setStatus("Pending");
                replacement.setSelectionDate(now);
            }

            // If there are more Pending entrants than replacements,
            // mark the remaining Pending entrants as No Response
            if (pendingEntrants.size() > notSelectedEntrants.size()) {
                for (int i = notSelectedEntrants.size(); i < pendingEntrants.size(); i++) {
                    WaitlistEntrant extraPending = pendingEntrants.get(i);
                    extraPending.setStatus("No Response");
                    createInvitationRevokedNotification(extraPending, event);
                }
            }

            db.collection("events").document(eventDocId)
                    .update("waitlist.waitlistEntrants", entrants)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Redraw completed: Pending entrants updated.", Toast.LENGTH_SHORT).show();
                        loadWaitlistFromDatabase(eventId);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to redraw entrants.", Toast.LENGTH_SHORT).show();
                        Log.e("EventWaitlistActivity", "Redraw failed", e);
                    });
        });
    }


    /**
     * Creates a Firestore notification indicating that an invitation
     * was revoked because the user did not respond.
     */
    private void createInvitationRevokedNotification(WaitlistEntrant entrant, Event event) {
        if (entrant == null || entrant.getUserId() == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", entrant.getUserId());
        notification.put("eventId", eventId);
        notification.put("eventName", event.getTitle());
        notification.put("created_at", new Date());
        notification.put("status", "UNREAD");
        notification.put("type", "invitation_revoked");
        notification.put("message",
                "Your invitation for \"" + event.getTitle() + "\" was revoked because you did not respond in time.");


        db.collection("notifications")
                .add(notification)
                .addOnFailureListener(e -> {
                    Log.e("Notifications", "Failed to create no response notification", e);
                });
    }

}
