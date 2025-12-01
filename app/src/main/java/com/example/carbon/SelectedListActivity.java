package com.example.carbon;

/**
 * Displays waitlist entrants marked as selected/pending and lets organizers broadcast reminders.
 * Outstanding issues: depends on live Firestore and lacks offline/empty-state caching.
 */

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectedListActivity extends AppCompatActivity {
    private Waitlist waitlist;
    private WaitlistAdapter adapter;
    private ArrayList<WaitlistEntrant> selectedEntrants = new ArrayList<>();
    private TextView emptyMessage;
    private Button notifySelectedButton;
    private String eventId;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_list);

        UIHelper.setupHeaderAndMenu(this);

        Intent intent = getIntent();
        eventId = intent.getStringExtra("EVENT_ID");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_selected);
        emptyMessage = findViewById(R.id.empty_message);

        adapter = new WaitlistAdapter(selectedEntrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        waitlist = new Waitlist();
        loadSelectedFromDatabase(eventId);

        // Set up "Notify Selected Entrants" button listener
        notifySelectedButton = findViewById(R.id.notify_selected_btn);
        notifySelectedButton.setOnClickListener(v -> showReminderDialog());
    }

    private void loadSelectedFromDatabase(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("events").whereEqualTo("uuid", eventId).limit(1);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                    Event event = document.toObject(Event.class);

                    if (event != null && event.getWaitlist() != null) {
                        this.currentEvent = event;
                        this.waitlist = event.getWaitlist();
                        List<WaitlistEntrant> entrants = this.waitlist.getWaitlistEntrants();

                        if (entrants != null && !entrants.isEmpty()) {
                            selectedEntrants.clear();

                            for (WaitlistEntrant entrant : entrants) {
                                if (entrant != null && !Objects.equals(entrant.getStatus(), "Not Selected")) {
                                    selectedEntrants.add(entrant);
                                }
                            }

                            adapter.notifyDataSetChanged();

                            if (selectedEntrants.isEmpty()) {
                                emptyMessage.setVisibility(View.VISIBLE);
                            } else {
                                emptyMessage.setVisibility(View.GONE);
                            }

                            Log.d("Selected DB", "Loaded " + selectedEntrants.size() + " selected entrants.");
                        } else {
                            emptyMessage.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "No entrants found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Waitlist missing in this event.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                Log.e("Selected DB", "Error loading: ", task.getException());
            }
        });
    }

    /**
     * Shows a dialog for the organizer to enter a reminder message to send to all selected entrants
     */
    private void showReminderDialog() {
        if (currentEvent == null) {
            Toast.makeText(this, "Event information not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_broadcast_message, null);
        EditText messageEditText = dialogView.findViewById(R.id.et_broadcast_message);
        
        // Pre-fill with template message
        String templateMessage = "Reminder: " + currentEvent.getTitle() + " is coming up!\n\n";
        messageEditText.setText(templateMessage);
        messageEditText.setSelection(templateMessage.length()); // Place cursor at end

        new AlertDialog.Builder(this)
                .setTitle("Notify Selected Entrants")
                .setView(dialogView)
                .setPositiveButton("Send", (dialog, which) -> {
                    String message = messageEditText.getText().toString().trim();
                    if (message.isEmpty() || message.equals(templateMessage.trim())) {
                        Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                    } else {
                        sendReminderToSelected(message);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Sends a reminder notification to all selected entrants
     * @param message the reminder message to send
     */
    private void sendReminderToSelected(String message) {
        if (selectedEntrants.isEmpty()) {
            Toast.makeText(this, "No selected entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseNotificationService notificationService = new FirebaseNotificationService();
        
        notificationService.broadcastNotificationToSelected(
                eventId,
                message,
                () -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Reminder sent to " + selectedEntrants.size() + " selected entrants", Toast.LENGTH_SHORT).show();
                    });
                },
                e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Failed to send reminders: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("SelectedListActivity", "Failed to send reminder notifications", e);
                    });
                }
        );
    }
}
