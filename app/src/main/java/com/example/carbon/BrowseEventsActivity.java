package com.example.carbon;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carbon.databinding.ActivityBrowseEventsBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BrowseEventsActivity extends AppCompatActivity {

    private ActivityBrowseEventsBinding binding;
    private EventsAdapter adapter;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBrowseEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup RecyclerView
        RecyclerView rv = binding.recyclerEvents;
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventsAdapter();
        rv.setAdapter(adapter);

        adapter.setEditModeListener(() -> toggleEditMode());



        // Add Delete listener
        adapter.setDeleteListener((event, position) -> {
            deleteEvent(event, position);
        });

        // Load the events
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Snackbar.make(binding.getRoot(), "Failed to load events", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    List<Event> eventList = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                eventList.add(event);
                            }
                        }
                    }
                    adapter.updateList(eventList);
                });

        ImageButton backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        adapter.setEditMode(isEditMode);
        String msg = isEditMode ? "Switched to Admin Mode" : "User Mode";
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
    }

    private void deleteEvent(Event event, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .whereEqualTo("uuid", event.getUuid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("events").document(docId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    adapter.updateList(new ArrayList<>(adapter.getEvents()));
                                    Snackbar.make(binding.getRoot(), "Event deleted", Snackbar.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Snackbar.make(binding.getRoot(), "Delete failed", Snackbar.LENGTH_LONG).show();
                                });
                    }
                });
    }
}