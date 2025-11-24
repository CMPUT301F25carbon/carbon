package com.example.carbon;

import android.os.Bundle;
import android.util.Log;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE";
    public static final String EXTRA_EVENT_DATE = "EXTRA_EVENT_DATE";     // e.g., "05/12/2025"
    public static final String EXTRA_EVENT_COUNTS = "EXTRA_EVENT_COUNTS"; // e.g., "11 registrations / 5 spots"

    private TextView tvTitle, tvDate, tvCounts;
    private EditText etSampleN;
    private Button btnEdit, btnCancel, btnSampleN;
    private RecyclerView rvRegistrants;

    private String eventId;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        UIHelper.setupHeaderAndMenu(this);

        tvTitle  = findViewById(R.id.tv_event_title);
        tvDate   = findViewById(R.id.tv_event_date);
        tvCounts = findViewById(R.id.tv_event_counts);
        etSampleN = findViewById(R.id.et_sample_n);
        btnEdit = findViewById(R.id.btn_edit);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSampleN = findViewById(R.id.btn_sample_n);
        rvRegistrants = findViewById(R.id.rv_registrants);

        // Get data from intent (safe defaults)
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        String title  = getIntent().getStringExtra(EXTRA_EVENT_TITLE);
        String date   = getIntent().getStringExtra(EXTRA_EVENT_DATE);
        String counts = getIntent().getStringExtra(EXTRA_EVENT_COUNTS);

        tvTitle.setText(title != null ? title : "Event");
        tvDate.setText(date != null ? date : "");
        tvCounts.setText(counts != null ? counts : "");

        // Simple list placeholder; wire real adapter later
        rvRegistrants.setLayoutManager(new LinearLayoutManager(this));
        rvRegistrants.setAdapter(new UsersAdapter()); // you already have UsersAdapter; empty list is okay

        bindActions();
    }

    private void bindActions() {
        btnEdit.setOnClickListener(v -> {
            // TODO: navigate to your edit screen with eventId when that flow is ready
            Toast.makeText(this, "Edit not implemented yet", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Cancel Event?")
                .setMessage("This will cancel the event for all registrants.")
                .setPositiveButton("Confirm", (d, which) -> {
                    // TODO: cancel in repo when ready
                    Toast.makeText(this, "Event cancel flow pending", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Keep", null)
                .show());

        btnSampleN.setOnClickListener(this::onSampleNClicked);
    }

    private void onSampleNClicked(View v) {
        String s = etSampleN.getText().toString().trim();
        if (s.isEmpty()) {
            Toast.makeText(this, "Enter N to sample", Toast.LENGTH_SHORT).show();
            return;
        }
        int n;
        try { n = Integer.parseInt(s); } catch (NumberFormatException e) { n = 0; }
        if (n <= 0) {
            Toast.makeText(this, "N must be ≥ 1", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID not found.", Toast.LENGTH_LONG).show();
            return;
        }

        // Perform the draw and send notifications
        performDrawAndNotify(n);
    }

    /**
     * Performs a random draw of N entrants from the waitlist and sends notifications
     * @param n The number of entrants to select
     */
    private void performDrawAndNotify(int n) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("events").whereEqualTo("uuid", eventId).limit(1);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                Event event = document.toObject(Event.class);

                if (event != null && event.getWaitlist() != null) {
                    List<WaitlistEntrant> allEntrants = event.getWaitlist().getWaitlistEntrants();
                    
                    if (allEntrants == null || allEntrants.isEmpty()) {
                        Toast.makeText(this, "No entrants in waitlist.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Filter to only "Not Selected" entrants
                    List<WaitlistEntrant> availableEntrants = new ArrayList<>();
                    for (WaitlistEntrant entrant : allEntrants) {
                        if (entrant != null && Objects.equals(entrant.getStatus(), "Not Selected")) {
                            availableEntrants.add(entrant);
                        }
                    }

                    if (availableEntrants.isEmpty()) {
                        Toast.makeText(this, "No available entrants to select.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Randomly select N entrants (or all if less than N)
                    int toSelect = Math.min(n, availableEntrants.size());
                    Collections.shuffle(availableEntrants);
                    List<WaitlistEntrant> selectedEntrants = availableEntrants.subList(0, toSelect);

                    // Update status to "Pending" and send notifications
                    updateEntrantsAndSendNotifications(document.getId(), selectedEntrants, event, toSelect);
                } else {
                    Toast.makeText(this, "Waitlist not found for this event.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates selected entrants status to "Pending" and sends notifications
     */
    private void updateEntrantsAndSendNotifications(String eventDocId, List<WaitlistEntrant> selectedEntrants, Event event, int count) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        NotificationService notificationService = new FirebaseNotificationService();
        
        // Update each selected entrant's status to "Pending"
        for (WaitlistEntrant entrant : selectedEntrants) {
            entrant.setStatus("Pending");
        }

        // Update the event in Firestore
        db.collection("events").document(eventDocId)
                .update("waitlist.waitlistEntrants", event.getWaitlist().getWaitlistEntrants())
                .addOnSuccessListener(aVoid -> {
                    // Send notifications to all selected entrants
                    int[] successCount = {0};
                    int[] failCount = {0};
                    
                    for (WaitlistEntrant entrant : selectedEntrants) {
                        Notification notification = new Notification(
                                null, // id will be set by Firebase
                                entrant.getUserId(),
                                eventId,
                                event.getTitle(),
                                "You have been selected for the event: " + event.getTitle() + ". Please accept or decline.",
                                NotificationStatus.UNREAD,
                                new Date(),
                                "chosen" // type for chosen entrants
                        );

                        notificationService.sendNotification(notification, 
                            () -> {
                                successCount[0]++;
                                if (successCount[0] + failCount[0] == selectedEntrants.size()) {
                                    showDrawCompleteDialog(count, successCount[0], failCount[0]);
                                }
                            },
                            e -> {
                                failCount[0]++;
                                Log.e("EventDetails", "Failed to send notification to " + entrant.getUserId(), e);
                                if (successCount[0] + failCount[0] == selectedEntrants.size()) {
                                    showDrawCompleteDialog(count, successCount[0], failCount[0]);
                                }
                            }
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update waitlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("EventDetails", "Failed to update waitlist", e);
                });
    }

    /**
     * Shows dialog after draw completion
     */
    private void showDrawCompleteDialog(int totalSelected, int successCount, int failCount) {
        String message = totalSelected + " participants have been randomly selected";
        if (successCount > 0) {
            message += "\n" + successCount + " notification(s) sent successfully";
        }
        if (failCount > 0) {
            message += "\n" + failCount + " notification(s) failed to send";
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Entrants Selected")
                .setMessage(message)
                .setPositiveButton("To Notifications", (d, w) -> {
                    startActivity(new android.content.Intent(this, NotificationActivity.class));
                })
                .setNegativeButton("Close", null)
                .show();
    }
}

