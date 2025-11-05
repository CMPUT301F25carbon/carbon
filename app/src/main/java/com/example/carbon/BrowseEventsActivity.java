package com.example.carbon;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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
    private EventsAdapter eventsAdapter;
    private UsersAdapter usersAdapter;
    private boolean isEditMode = false;
    private boolean isProfilesView = false;
    private ArrayList<Event> currentEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBrowseEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

<<<<<<< HEAD
        UIHelper.setupHeaderAndMenu(this);

=======
>>>>>>> origin/main
        // Setup the RecyclerView
        RecyclerView rv = binding.recyclerEvents;
        rv.setLayoutManager(new LinearLayoutManager(this));
        eventsAdapter = new EventsAdapter(currentEvents);
        usersAdapter = new UsersAdapter();
        rv.setAdapter(eventsAdapter);

        // Toggle switch for profiles in admin mode between events and profiles view
        SwitchCompat toggleProfiles = binding.toggleProfiles;
        toggleProfiles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isProfilesView = isChecked;
            if (isProfilesView) {
                loadUsers();
            } else {
                loadEvents();
            }
        });

        // Set long press listener to swich to admin mode
        eventsAdapter.setLongPressListener(this::toggleEditMode);

        // Set the delete listener for events
        eventsAdapter.setDeleteListener(this::deleteEvent);

        // Load the initial events
        loadEvents();
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        eventsAdapter.setEditMode(isEditMode);
        binding.toggleProfiles.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        Snackbar.make(binding.getRoot(), isEditMode ? "Admin Mode" : "Normal Mode", Snackbar.LENGTH_SHORT).show();
    }

    private void loadEvents() {
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
                    eventsAdapter.updateList(eventList);
                    binding.recyclerEvents.setAdapter(eventsAdapter);
                });
    }

    private void loadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Snackbar.make(binding.getRoot(), "Failed to load profiles", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    List<User> userList = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                userList.add(user);
                            }
                        }
                    }
                    usersAdapter.updateList(userList);
                    binding.recyclerEvents.setAdapter(usersAdapter);
                });
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
                                    Snackbar.make(binding.getRoot(), "Event deleted", Snackbar.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Snackbar.make(binding.getRoot(), "Delete failed", Snackbar.LENGTH_SHORT).show();
                                });
                    }
                });
    }
}