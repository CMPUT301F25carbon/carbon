package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE";
    public static final String EXTRA_EVENT_DATE = "EXTRA_EVENT_DATE";     // e.g., "05/12/2025"
    public static final String EXTRA_EVENT_COUNTS = "EXTRA_EVENT_COUNTS"; // e.g., "11 registrations / 5 spots"

    private TextView tvTitle, tvDate, tvCounts;
    private Button btnEdit, btnCancel, btnSample; // renamed variable only (id stays the same)
    private RecyclerView rvRegistrants;

    private String eventId;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        UIHelper.setupHeaderAndMenu(this);

        tvTitle  = findViewById(R.id.tv_event_title);
        tvDate   = findViewById(R.id.tv_event_date);
        tvCounts = findViewById(R.id.tv_event_counts);
        btnEdit = findViewById(R.id.btn_edit);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSample = findViewById(R.id.btn_sample_n); // same XML id
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
        rvRegistrants.setAdapter(new UsersAdapter()); // existing adapter; empty list ok

        bindActions();
    }

    private void bindActions() {
        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Edit not implemented yet", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Cancel Event?")
                .setMessage("This will cancel the event for all registrants.")
                .setPositiveButton("Confirm", (d, which) -> {
                    Toast.makeText(this, "Event cancel flow pending", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Keep", null)
                .show());

        btnSample.setOnClickListener(this::onSampleClicked);
    }

    private void onSampleClicked(View v) {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query q = db.collection("events")
                .whereEqualTo("uuid", eventId)
                .limit(1);

        q.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().isEmpty()) {
                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                return;
            }

            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
            Event event = doc.toObject(Event.class);
            if (event == null) {
                Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer totalSpots = event.getTotalSpots();
            if (totalSpots == null || totalSpots <= 0) {
                Toast.makeText(this, "No available spots set for this event", Toast.LENGTH_SHORT).show();
                return;
            }

            if (event.getWaitlist() == null || event.getWaitlist().getWaitlistEntrants() == null) {
                Toast.makeText(this, "No waitlist found for this event", Toast.LENGTH_SHORT).show();
                return;
            }

            List<WaitlistEntrant> entrants = event.getWaitlist().getWaitlistEntrants();
            if (entrants.isEmpty()) {
                Toast.makeText(this, "No registrants to sample", Toast.LENGTH_SHORT).show();
                return;
            }

            // Shuffle entrants and select top K = min(totalSpots, entrants.size())
            int k = Math.min(totalSpots, entrants.size());
            Collections.shuffle(entrants, new Random());

            // Clear previous selections, then mark winners
            for (int i = 0; i < entrants.size(); i++) {
                entrants.get(i).setSelected(i < k);
            }

            // Persist only the waitlist back to the same document
            doc.getReference()
                    .update("waitlist", event.getWaitlist())
                    .addOnSuccessListener(aVoid -> {
                        String msg = String.format(Locale.US, "Sampled %d participant%s", k, k == 1 ? "" : "s");
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                        // Optional: jump to SelectedListActivity so organizer can see winners
                        Intent intent = new Intent(this, SelectedListActivity.class);
                        intent.putExtra("EVENT_ID", eventId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save selection", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}

