package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class SelectedListActivity extends AppCompatActivity {
    private Waitlist waitlist;
    private WaitlistAdapter adapter;
    private ArrayList<WaitlistEntrant> selectedEntrants = new ArrayList<>();
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_list);

        UIHelper.setupHeaderAndMenu(this);

        Intent intent = getIntent();
        String eventId = intent.getStringExtra("EVENT_ID");

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
                        this.waitlist = event.getWaitlist();
                        List<WaitlistEntrant> entrants = this.waitlist.getWaitlistEntrants();

                        if (entrants != null && !entrants.isEmpty()) {
                            selectedEntrants.clear();

                            for (WaitlistEntrant entrant : entrants) {
                                if (entrant != null && entrant.isSelected()) {
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
}
