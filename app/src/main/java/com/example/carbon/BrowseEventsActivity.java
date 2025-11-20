package com.example.carbon;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.carbon.databinding.ActivityBrowseEventsBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class BrowseEventsActivity extends AppCompatActivity {
    private ActivityBrowseEventsBinding binding;
    private EventsAdapter eventsAdapter;
    private UsersAdapter usersAdapter;
    private boolean isEditMode = false;
    private boolean isProfilesView = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private ArrayList<Event> currentEvents = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UIHelper.setupHeaderAndMenu(this);
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
        eventsAdapter.setOnItemClickListener(event -> {
            // ✅ Open Event Details page on tap (UML: Home → Event Details)
            Intent intent = new Intent(BrowseEventsActivity.this, EventDetailsActivity.class);
            // Pass the event data to the details activity
            intent.putExtra("EXTRA_EVENT_TITLE", event.getTitle());
            intent.putExtra("EXTRA_EVENT_DATE", dateFormat.format(event.getEventDate()));
            intent.putExtra("EXTRA_EVENT_COUNTS", event.getTotalSpots() + " spots");
            // Pass the event's unique ID so the details page can fetch more info if needed
            intent.putExtra("EXTRA_EVENT_ID", event.getUuid());
            startActivity(intent);
        });
        // Load the initial events
        loadEvents();
    }

    private void deleteUser(User user, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("users").document(docId).delete()
                                .addOnSuccessListener(aVoid -> Snackbar.make(binding.getRoot(), "Profile deleted", Snackbar.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Snackbar.make(binding.getRoot(), "Delete failed", Snackbar.LENGTH_SHORT).show());
                    }
                });
    }

    private void toggleEditMode() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!isEditMode) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                String role = doc.getString("role");
                if ("admin".equals(role)) {
                    isEditMode = true;
                    eventsAdapter.setEditMode(isEditMode);
                    binding.toggleProfiles.setVisibility(View.VISIBLE);
                    Snackbar.make(binding.getRoot(), "Admin Mode", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(binding.getRoot(), "Only admins can enter Admin Mode", Snackbar.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Snackbar.make(binding.getRoot(), "Failed to check role", Snackbar.LENGTH_SHORT).show();
            });
        } else {
            isEditMode = false;
            eventsAdapter.setEditMode(isEditMode);
            binding.toggleProfiles.setVisibility(View.GONE);
            Snackbar.make(binding.getRoot(), "Normal Mode", Snackbar.LENGTH_SHORT).show();
        }
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
                    usersAdapter.setDeleteListener(this::deleteUser);
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