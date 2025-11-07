package com.example.carbon;

import android.os.Bundle;
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

public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE";
    public static final String EXTRA_EVENT_DATE = "EXTRA_EVENT_DATE";     // e.g., "05/12/2025"
    public static final String EXTRA_EVENT_COUNTS = "EXTRA_EVENT_COUNTS"; // e.g., "11 registrations / 5 spots"

    public static final String EXTRA_WAITLIST_COUNT = "EXTRA_WAITLIST_COUNT";


    private TextView tvTitle, tvDate, tvCounts, tvWaitlistCount;
    private EditText etSampleN;
    private Button btnEdit, btnCancel, btnSampleN;
    private RecyclerView rvRegistrants;

    private String eventId;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Waitlist waitlist;
    private List<WaitlistEntrant> displayedEntrants = new ArrayList<>();
    private WaitlistAdapter adapter; // You'll create this below


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        UIHelper.setupHeaderAndMenu(this);

        tvTitle  = findViewById(R.id.tv_event_title);
        tvDate   = findViewById(R.id.tv_event_date);
        tvCounts = findViewById(R.id.tv_event_counts);
        tvWaitlistCount = findViewById(R.id.tv_waitlist_count);
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
        int waitlistCount = getIntent().getIntExtra(EXTRA_WAITLIST_COUNT, 0);

        tvTitle.setText(title != null ? title : "Event");
        tvDate.setText(date != null ? date : "");
        tvCounts.setText(counts != null ? counts : "");
        tvWaitlistCount.setText("Waitlist: " + waitlistCount + " entrant" + (waitlistCount == 1 ? "" : "s"));

        // Simple list placeholder; wire real adapter later
        rvRegistrants.setLayoutManager(new LinearLayoutManager(this));
        rvRegistrants.setAdapter(new UsersAdapter()); // you already have UsersAdapter; empty list is okay


        adapter = new WaitlistAdapter(displayedEntrants);
        rvRegistrants.setAdapter(adapter);
        loadWaitlistFromDatabase(eventId);

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

        // This is just the **Notify Chosen Entrants** confirmation UI.
        // Actual sampling & invites will be wired to repo later.
        new AlertDialog.Builder(this)
                .setTitle("Entrants Selected")
                .setMessage(n + " participants have been randomly selected and sent an invitation!")
                .setPositiveButton("To Invitations", (d, w) -> {
                    // Go to your notifications screen per UML
                    startActivity(new android.content.Intent(this, NotificationActivity.class));
                })
                .setNegativeButton("Close", null)
                .show();
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

                    // Update TextView count dynamically
                    tvWaitlistCount.setText("Waitlist: " + displayedEntrants.size() + " entrant" + (displayedEntrants.size() == 1 ? "" : "s"));

                } else {
                    Toast.makeText(this, "Waitlist data missing.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}

