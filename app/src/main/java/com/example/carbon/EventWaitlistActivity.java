package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventWaitlistActivity extends AppCompatActivity {
    private Waitlist waitlist;
    private WaitlistAdapter adapter;
    private ArrayList<WaitlistEntrant> displayedEntrants = new ArrayList<>();
    private Button btnJoinWaitlist, btnLeaveWaitlist;
    private String eventId;

    private TextView tvWaitlistCount;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_waitlist);

        UIHelper.setupHeaderAndMenu(this);
        db = FirebaseFirestore.getInstance();

        // Get event ID
        Intent intent = getIntent();
        eventId = intent.getStringExtra("EVENT_ID");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Recycler setup
        RecyclerView recyclerView = findViewById(R.id.recycler_waitlist);
        adapter = new WaitlistAdapter(displayedEntrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Buttons
        btnJoinWaitlist = findViewById(R.id.btn_join_waitlist);
        btnLeaveWaitlist = findViewById(R.id.btn_leave_waitlist);
        tvWaitlistCount = findViewById(R.id.tv_waitlist_count);


        // Button actions
        btnJoinWaitlist.setOnClickListener(v -> joinWaitlist());
        btnLeaveWaitlist.setOnClickListener(v -> leaveWaitlist());

        // Initial load
        loadWaitlistFromDatabase(eventId);
    }

    private void loadWaitlistFromDatabase(String eventId) {
        Query query = db.collection("events").whereEqualTo("uuid", eventId).limit(1);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                Event event = document.toObject(Event.class);

                if (event != null && event.getWaitlist() != null) {
                    this.waitlist = event.getWaitlist();
                    List<WaitlistEntrant> entrants = this.waitlist.getWaitlistEntrants();

                    displayedEntrants.clear();
                    if (entrants != null) displayedEntrants.addAll(entrants);
                    adapter.notifyDataSetChanged();
                    if (tvWaitlistCount != null) {
                        tvWaitlistCount.setText("Waitlist: " + displayedEntrants.size() + " entrant" + (displayedEntrants.size() == 1 ? "" : "s"));
                    }


                    Log.d("Waitlist DB", "Loaded " + displayedEntrants.size() + " entrants.");
                } else {
                    Toast.makeText(this, "Waitlist data missing.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Add current user to waitlist ---
    private void joinWaitlist() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("events").whereEqualTo("uuid", eventId).limit(1).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        String docId = doc.getId();
                        Event event = doc.toObject(Event.class);
                        if (event == null || event.getWaitlist() == null) return;

                        Waitlist waitlist = event.getWaitlist();
                        if (waitlist.getWaitlistEntrants() == null)
                            waitlist.setWaitlistEntrants(new ArrayList<>()); // ensure non-null

                        if (!waitlist.joinWaitlist(userId)) {
                            Toast.makeText(this, "You’re already on the waitlist or it’s closed/full.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        db.collection("events").document(docId)
                                .update("waitlist.waitlistEntrants", waitlist.getWaitlistEntrants())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Joined waitlist!", Toast.LENGTH_SHORT).show();
                                    loadWaitlistFromDatabase(eventId);
                                });
                    }
                });
    }



    private void leaveWaitlist() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("events").whereEqualTo("uuid", eventId).limit(1).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        String docId = doc.getId();
                        Event event = doc.toObject(Event.class);
                        if (event == null || event.getWaitlist() == null) return;

                        Waitlist waitlist = event.getWaitlist();

                        if (!waitlist.leaveWaitlist(userId)) {
                            Toast.makeText(this, "You’re not on the waitlist.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        db.collection("events").document(docId)
                                .update("waitlist.waitlistEntrants", waitlist.getWaitlistEntrants())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Left waitlist.", Toast.LENGTH_SHORT).show();
                                    // ✅ ADD THIS LINE BELOW
                                    loadWaitlistFromDatabase(eventId); // 🔄 refresh waitlist after leaving
                                });
                    }
                });
    }
}

