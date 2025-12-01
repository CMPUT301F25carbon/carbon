package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import java.util.Objects;


/**
 * Activity to display a list of waitlist entrants who have been cancelled
 * for a specific event.
 *
 * <p>The activity retrieves the event ID ,
 * fetches the corresponding event and its waitlist from Firebase Firestore,
 * and filters out entrants with the status "Cancelled".</p>
 *
 * <p>If there are no cancelled entrants, a message is displayed indicating that
 * no cancelled entrants were found.</p>
 */
public class CancelledListActivity extends AppCompatActivity {

    private Waitlist waitlist;
    private WaitlistAdapter adapter;
    private final ArrayList<WaitlistEntrant> cancelledEntrants = new ArrayList<>();
    private TextView emptyMessage;
    private String eventId;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelled_list);

        UIHelper.setupHeaderAndMenu(this);

        Intent intent = getIntent();
        eventId = intent.getStringExtra("EVENT_ID");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_cancelled);
        emptyMessage = findViewById(R.id.empty_message_cancelled);

        adapter = new WaitlistAdapter(cancelledEntrants); // adapter should show reason field
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        waitlist = new Waitlist();
        loadCancelledFromDatabase(eventId);
    }

    private void loadCancelledFromDatabase(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("events")
                .whereEqualTo("uuid", eventId)
                .limit(1);

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
                            cancelledEntrants.clear();

                            for (WaitlistEntrant entrant : entrants) {
                                if (entrant != null && Objects.equals(entrant.getStatus(), "Cancelled")) {
                                    cancelledEntrants.add(entrant);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            emptyMessage.setVisibility(cancelledEntrants.isEmpty() ? View.VISIBLE : View.GONE);
                            if (cancelledEntrants.isEmpty()) {
                                emptyMessage.setText("No cancelled entrants found.");
                            }

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
            }
        });
    }
}
