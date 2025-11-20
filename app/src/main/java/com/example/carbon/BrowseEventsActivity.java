package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carbon.databinding.ActivityBrowseEventsBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * BrowseEventsActivity is the main admin dashboard for viewing and managing:
 * - Upcoming events
 * - User profiles (admin only)
 * - System notifications (admin only)
 *
 * Features:
 * - Long-press any event to enter Admin Mode
 * - Three-tab interface appears in Admin Mode: Events | Profiles | Notifications
 * - Real-time Firestore listeners for all collections
 * - Delete functionality for events and users
 *
 */
public class BrowseEventsActivity extends AppCompatActivity {

    private ActivityBrowseEventsBinding binding;

    // Adapters for the three different views
    private EventsAdapter eventsAdapter;
    private UsersAdapter usersAdapter;
    private NotificationsAdapter notificationsAdapter;

    // Admin state
    private boolean isEditMode = false;

    // Date formatter used when opening event details
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    // Enum to track which tab is currently visible
    private enum ViewType { EVENTS, PROFILES, NOTIFICATIONS }
    private ViewType currentView = ViewType.EVENTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UIHelper.setupHeaderAndMenu(this);
        setupRecyclerView();
        setupAdminTabButtons();
        loadEvents(); // Default view on launch
    }

    /**
     * Initializes the RecyclerView with a LinearLayoutManager and creates all adapters.
     */
    private void setupRecyclerView() {
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(this));

        eventsAdapter = new EventsAdapter(new ArrayList<>());
        usersAdapter = new UsersAdapter();
        notificationsAdapter = new NotificationsAdapter();

        // Start with events adapter
        binding.recyclerEvents.setAdapter(eventsAdapter);

        // Long-press on any event card activates admin mode
        eventsAdapter.setLongPressListener(this::toggleEditMode);
        eventsAdapter.setDeleteListener(this::deleteEvent);
        eventsAdapter.setOnItemClickListener(this::openEventDetails);
    }

    /**
     * Sets up click listeners for the three admin tab buttons.
     */
    private void setupAdminTabButtons() {
        binding.btnTabEvents.setOnClickListener(v -> switchTo(ViewType.EVENTS));
        binding.btnTabProfiles.setOnClickListener(v -> switchTo(ViewType.PROFILES));
        binding.btnTabNotifications.setOnClickListener(v -> switchTo(ViewType.NOTIFICATIONS));
    }

    /**
     * Switches the displayed content and highlights the selected tab.
     *
     * @param type The target view (Events, Profiles, or Notifications)
     */
    private void switchTo(ViewType type) {
        currentView = type;

        if (type == ViewType.EVENTS) {
            loadEvents();
        } else if (type == ViewType.PROFILES) {
            loadUsers();
        } else if (type == ViewType.NOTIFICATIONS) {
            loadNotifications();
        }

        highlightSelectedTab(type);
    }

    /**
     * Highlights the currently active tab button using the app's gold theme color.
     *
     * @param activeType The currently selected view type
     */
    private void highlightSelectedTab(ViewType activeType) {
        resetTabColors();

        // Your exact gold color from the design (#F0DAA0)
        int gold = 0xFFF0DAA0;

        MaterialButton activeButton = activeType == ViewType.EVENTS ? binding.btnTabEvents :
                activeType == ViewType.PROFILES ? binding.btnTabProfiles :
                        binding.btnTabNotifications;

        activeButton.setBackgroundColor(gold);
        activeButton.setTextColor(0xFF4A148C); // Dark purple text for perfect contrast on gold
    }

    /**
     * Resets all tab buttons to transparent background (outlined style).
     */
    private void resetTabColors() {

        int transparent = 0x00000000;

        binding.btnTabEvents.setBackgroundColor(transparent);
        binding.btnTabEvents.setTextColor(0xFFF0DAA0);   // Gold text when not selected

        binding.btnTabProfiles.setBackgroundColor(transparent);
        binding.btnTabProfiles.setTextColor(0xFFF0DAA0);

        binding.btnTabNotifications.setBackgroundColor(transparent);
        binding.btnTabNotifications.setTextColor(0xFFF0DAA0);
    }

    /**
     * Toggles Admin Mode on/off.
     * Activated by long-pressing any event in the list.
     */
    private void toggleEditMode() {
        if (isEditMode) {
            exitAdminMode();
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if ("admin".equals(documentSnapshot.getString("role"))) {
                        enterAdminMode();
                    } else {
                        Snackbar.make(binding.getRoot(), "Only admins can enter Admin Mode", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Snackbar.make(binding.getRoot(), "Failed to verify role", Snackbar.LENGTH_SHORT).show());
    }

    /** Enters Admin Mode – shows the three tabs and enables edit features */
    private void enterAdminMode() {
        isEditMode = true;
        eventsAdapter.setEditMode(true);
        binding.adminTabLayout.setVisibility(android.view.View.VISIBLE);
        Snackbar.make(binding.getRoot(), "Admin Mode Activated", Snackbar.LENGTH_SHORT).show();
        highlightSelectedTab(currentView);
    }

    /** Exits Admin Mode – hides tabs and disables editing */
    private void exitAdminMode() {
        isEditMode = false;
        eventsAdapter.setEditMode(false);
        binding.adminTabLayout.setVisibility(android.view.View.GONE);
        Snackbar.make(binding.getRoot(), "Normal Mode", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Opens the detailed view of a selected event.
     *
     * @param event The event that was tapped
     */
    private void openEventDetails(Event event) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EXTRA_EVENT_TITLE", event.getTitle());
        intent.putExtra("EXTRA_EVENT_DATE", dateFormat.format(event.getEventDate()));
        intent.putExtra("EXTRA_EVENT_COUNTS", event.getTotalSpots() + " spots");
        intent.putExtra("EXTRA_EVENT_ID", event.getUuid());
        startActivity(intent);
    }

    /** Loads and displays all events from Firestore in real-time */
    private void loadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;

                    List<Event> eventList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) eventList.add(event);
                    }
                    eventsAdapter.updateList(eventList);
                    binding.recyclerEvents.setAdapter(eventsAdapter);
                });
    }

    /** Loads and displays all user profiles (admin view) */
    private void loadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;

                    List<User> userList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) userList.add(user);
                    }
                    usersAdapter.updateList(userList);
                    usersAdapter.setDeleteListener(this::deleteUser);
                    binding.recyclerEvents.setAdapter(usersAdapter);
                });
    }

    /** Loads and displays all notifications from the notifications collection */
    private void loadNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Snackbar.make(binding.getRoot(), "Failed to load notifications", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    List<Notification> notificationList = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Notification notification = doc.toObject(Notification.class);
                            if (notification != null) notificationList.add(notification);
                        }
                    }

                    notificationsAdapter.updateList(notificationList);
                    binding.recyclerEvents.setAdapter(notificationsAdapter);
                });
    }

    /**
     * Deletes a user profile from Firestore.
     *
     * @param user     The user to delete
     * @param position Adapter position (unused here but kept for consistency)
     */
    private void deleteUser(User user, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("users").document(docId).delete()
                                .addOnSuccessListener(aVoid ->
                                        Snackbar.make(binding.getRoot(), "Profile deleted", Snackbar.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Snackbar.make(binding.getRoot(), "Delete failed", Snackbar.LENGTH_SHORT).show());
                    }
                });
    }

    /**
     * Deletes an event from Firestore.
     *
     * @param event    The event to delete
     * @param position Adapter position (unused here but kept for consistency)
     */
    private void deleteEvent(Event event, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .whereEqualTo("uuid", event.getUuid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        db.collection("events").document(docId).delete()
                                .addOnSuccessListener(aVoid ->
                                        Snackbar.make(binding.getRoot(), "Event deleted", Snackbar.LENGTH_SHORT).show());
                    }
                });
    }
}