package com.example.carbon;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carbon.databinding.ActivityBrowseEventsBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Main admin dashboard for browsing and managing events, users, notifications, and event posters.
 * Features admin-only tabs activated via long-press on any event.
 * Posters are now loaded directly from Firebase Storage (event_posters/) instead of Firestore.
 */
public class BrowseEventsActivity extends AppCompatActivity {

    private ActivityBrowseEventsBinding binding;

    private EventsAdapter eventsAdapter;
    private UsersAdapter usersAdapter;
    private NotificationsAdapter notificationsAdapter;
    private PostersAdapter postersAdapter;

    private boolean isEditMode = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    private enum ViewType { EVENTS, PROFILES, NOTIFICATIONS, POSTERS }
    private ViewType currentView = ViewType.EVENTS;

    private Spinner spinnerCategory, spinnerLocation;
    private Button btnFilterDate, btnClearFilters;
    private String selectedCategory = null;
    private String selectedLocation = null;
    private Date selectedDate = null;
    private List<Event> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UIHelper.setupHeaderAndMenu(this);
        setupRecyclerView();
        setupAdminTabButtons();
        setupFilters();
        loadEvents();
    }

    /** Initializes RecyclerView and creates all required adapters */
    private void setupRecyclerView() {
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(this));

        eventsAdapter = new EventsAdapter(new ArrayList<>());
        usersAdapter = new UsersAdapter();
        notificationsAdapter = new NotificationsAdapter();
        postersAdapter = new PostersAdapter();

        binding.recyclerEvents.setAdapter(eventsAdapter);

        eventsAdapter.setLongPressListener(this::toggleEditMode);
        eventsAdapter.setDeleteListener(this::deleteEvent);
        eventsAdapter.setOnItemClickListener(this::openEventDetails);
    }

    /** Sets up click listeners for the four admin navigation tabs */
    private void setupAdminTabButtons() {
        binding.btnTabEvents.setOnClickListener(v -> switchTo(ViewType.EVENTS));
        binding.btnTabProfiles.setOnClickListener(v -> switchTo(ViewType.PROFILES));
        binding.btnTabNotifications.setOnClickListener(v -> switchTo(ViewType.NOTIFICATIONS));
        binding.btnTabPosters.setOnClickListener(v -> switchTo(ViewType.POSTERS));
    }

    /** Switches between admin views (Events, Profiles, Notifications, Posters) */
    private void switchTo(ViewType type) {
        currentView = type;

        if (type == ViewType.EVENTS) {
            binding.recyclerEvents.setAdapter(eventsAdapter);
            binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
            loadEvents();
        } else if (type == ViewType.PROFILES) {
            binding.recyclerEvents.setAdapter(usersAdapter);
            binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
            loadUsers();
        } else if (type == ViewType.NOTIFICATIONS) {
            binding.recyclerEvents.setAdapter(notificationsAdapter);
            binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
            loadNotifications();
        } else if (type == ViewType.POSTERS) {
            binding.recyclerEvents.setAdapter(postersAdapter);
            binding.recyclerEvents.setLayoutManager(new GridLayoutManager(this, 2));
            loadPostersFromStorage();
        }

        highlightSelectedTab(type);
    }

    /** Applies gold highlight to the currently selected tab */
    private void highlightSelectedTab(ViewType activeType) {
        resetTabColors();
        int gold = 0xFFF0DAA0;

        MaterialButton activeButton = activeType == ViewType.EVENTS ? binding.btnTabEvents :
                activeType == ViewType.PROFILES ? binding.btnTabProfiles :
                        activeType == ViewType.NOTIFICATIONS ? binding.btnTabNotifications :
                                binding.btnTabPosters;

        activeButton.setBackgroundColor(gold);
        activeButton.setTextColor(0xFF4A148C);
    }

    /** Resets all tab button colors to outlined style */
    private void resetTabColors() {
        int transparent = 0x00000000;

        binding.btnTabEvents.setBackgroundColor(transparent);    binding.btnTabEvents.setTextColor(0xFFF0DAA0);
        binding.btnTabProfiles.setBackgroundColor(transparent);  binding.btnTabProfiles.setTextColor(0xFFF0DAA0);
        binding.btnTabNotifications.setBackgroundColor(transparent); binding.btnTabNotifications.setTextColor(0xFFF0DAA0);
        binding.btnTabPosters.setBackgroundColor(transparent);   binding.btnTabPosters.setTextColor(0xFFF0DAA0);
    }

    /** Toggles Admin Mode — activated by long-pressing any event */
    private void toggleEditMode() {
        if (isEditMode) {
            exitAdminMode();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if ("admin".equals(doc.getString("role"))) {
                        enterAdminMode();
                    } else {
                        Snackbar.make(binding.getRoot(), "Only admins can enter Admin Mode", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /** Activates Admin Mode — shows tabs and enables delete buttons */
    private void enterAdminMode() {
        isEditMode = true;
        eventsAdapter.setEditMode(true);
        postersAdapter.setEditMode(true);
        binding.adminTabLayout.setVisibility(View.VISIBLE);
        Snackbar.make(binding.getRoot(), "Admin Mode Activated", Snackbar.LENGTH_SHORT).show();
        highlightSelectedTab(currentView);
    }

    /** Deactivates Admin Mode and hides admin features */
    private void exitAdminMode() {
        isEditMode = false;
        eventsAdapter.setEditMode(false);
        postersAdapter.setEditMode(false);
        binding.adminTabLayout.setVisibility(View.GONE);
        Snackbar.make(binding.getRoot(), "Normal Mode", Snackbar.LENGTH_SHORT).show();
    }

    /** Opens detailed event view for the selected event */
    private void openEventDetails(Event event) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EXTRA_EVENT_TITLE", event.getTitle());
        intent.putExtra("EXTRA_EVENT_DATE", dateFormat.format(event.getEventDate()));
        intent.putExtra("EXTRA_EVENT_COUNTS", event.getTotalSpots() + " spots");
        intent.putExtra("EXTRA_EVENT_ID", event.getUuid());
        startActivity(intent);
    }

    /** Loads all images from Firebase Storage folder: event_posters/ */
    private void loadPostersFromStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference folderRef = storage.getReference().child("event_posters");

        folderRef.listAll().addOnSuccessListener(listResult -> {
            List<Poster> posterList = new ArrayList<>();

            if (listResult.getItems().isEmpty()) {
                runOnUiThread(() -> postersAdapter.updateList(posterList));
                return;
            }

            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    Poster poster = new Poster();
                    poster.setImageUrl(uri.toString());
                    poster.setEventId(item.getName());

                    posterList.add(poster);

                    if (posterList.size() == listResult.getItems().size()) {
                        runOnUiThread(() -> {
                            postersAdapter.updateList(posterList);
                            postersAdapter.setEditMode(isEditMode);
                            postersAdapter.setDeleteListener(this::deletePoster);
                        });
                    }
                }).addOnFailureListener(e -> {
                });
            }
        }).addOnFailureListener(e -> {
            Snackbar.make(binding.getRoot(), "Failed to load posters", Snackbar.LENGTH_LONG).show();
        });
    }

    /** Deletes a poster image from Firebase Storage */
    private void deletePoster(Poster poster, int position) {
        if (poster.getImageUrl() == null) return;

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(poster.getImageUrl());
        ref.delete()
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(binding.getRoot(), "Poster deleted", Snackbar.LENGTH_SHORT).show();
                    loadPostersFromStorage();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(binding.getRoot(), "Delete failed", Snackbar.LENGTH_SHORT).show();
                });
    }

    /** Initializes filter spinners and buttons */
    private void setupFilters() {
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerLocation = findViewById(R.id.spinner_location);
        btnFilterDate = findViewById(R.id.btn_filter_date);
        btnClearFilters = findViewById(R.id.btn_clear_filters);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All Categories", "Sports", "Entertainment", "Food", "Education", "Social", "Other"});
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All Locations"});
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);

        btnFilterDate.setOnClickListener(v -> showDatePicker());
        btnClearFilters.setOnClickListener(v -> clearFilters());
    }

    /** Displays date picker for filtering events by date */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            selectedDate = selected.getTime();
            btnFilterDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(selectedDate));
            applyFilters();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Clears all active filters */
    private void clearFilters() {
        selectedCategory = null;
        selectedLocation = null;
        selectedDate = null;
        spinnerCategory.setSelection(0);
        spinnerLocation.setSelection(0);
        btnFilterDate.setText("Date");
        applyFilters();
    }

    /** Applies current category, location, and date filters */
    private void applyFilters() {
        List<Event> filtered = new ArrayList<>();
        for (Event e : allEvents) {
            boolean match = true;

            if (selectedCategory != null && !selectedCategory.equals("All Categories") && !selectedCategory.equals(e.getCategory())) match = false;
            if (selectedLocation != null && !selectedLocation.equals("All Locations")) {
                String loc = e.getEventCity() != null ? e.getEventCity() : e.getEventLocation() != null ? e.getEventLocation() : "";
                if (!selectedLocation.equals(loc)) match = false;
            }
            if (selectedDate != null && e.getEventDate() != null) {
                Calendar ec = Calendar.getInstance(); ec.setTime(e.getEventDate());
                Calendar sc = Calendar.getInstance(); sc.setTime(selectedDate);
                if (ec.get(Calendar.YEAR) != sc.get(Calendar.YEAR) || ec.get(Calendar.MONTH) != sc.get(Calendar.MONTH) || ec.get(Calendar.DAY_OF_MONTH) != sc.get(Calendar.DAY_OF_MONTH))
                    match = false;
            }
            if (match) filtered.add(e);
        }
        eventsAdapter.updateList(filtered);
    }

    /** Loads all events from Firestore with real-time updates */
    private void loadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").addSnapshotListener((snapshots, error) -> {
            if (error != null) return;
            allEvents.clear();
            Set<String> locations = new HashSet<>(); locations.add("All Locations");

            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Event event = doc.toObject(Event.class);
                if (event != null) {
                    allEvents.add(event);
                    String loc = event.getEventCity() != null ? event.getEventCity() : event.getEventLocation();
                    if (loc != null && !loc.isEmpty()) locations.add(loc);
                }
            }

            spinnerLocation.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(locations)));
            applyFilters();
        });
    }

    /** Loads all user profiles for admin view */
    private void loadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").addSnapshotListener((s, e) -> {
            if (e != null) return;
            List<User> list = new ArrayList<>();
            for (DocumentSnapshot d : s.getDocuments()) {
                User u = d.toObject(User.class);
                if (u != null) list.add(u);
            }
            usersAdapter.updateList(list);
            usersAdapter.setDeleteListener(this::deleteUser);
        });
    }

    /** Loads all system notifications */
    private void loadNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications").addSnapshotListener((s, e) -> {
            if (e != null) {
                Snackbar.make(binding.getRoot(), "Failed to load notifications", Snackbar.LENGTH_LONG).show();
                return;
            }
            List<Notification> list = new ArrayList<>();
            for (DocumentSnapshot d : s.getDocuments()) {
                Notification n = d.toObject(Notification.class);
                if (n != null) list.add(n);
            }
            notificationsAdapter.updateList(list);
        });
    }

    /** Deletes a user profile from Firestore */
    private void deleteUser(User user, int pos) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", user.getEmail()).get()
                .addOnSuccessListener(q -> {
                    if (!q.isEmpty()) q.getDocuments().get(0).getReference().delete();
                });
    }

    /** Deletes an event from Firestore */
    private void deleteEvent(Event event, int pos) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").whereEqualTo("uuid", event.getUuid()).get()
                .addOnSuccessListener(q -> {
                    if (!q.isEmpty()) q.getDocuments().get(0).getReference().delete();
                });
    }
}