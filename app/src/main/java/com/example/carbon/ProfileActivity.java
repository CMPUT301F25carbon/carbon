package com.example.carbon;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Activity that displays the user's profile information, upcoming events,
 * and account management options. Users can edit their profile, delete
 * their account, view upcoming events, and toggle notification preferences.
 */


public class ProfileActivity extends AppCompatActivity {

    /**
     * Adapter for displaying upcoming events in a RecyclerView
     */
    private UpcomingEventsAdapter upcomingEventsAdapter;
    private ImageView avatarView;
    private ActivityResultLauncher<Intent> avatarPickerLauncher;
    private String avatarUrl;

    /**
     * Called when the activity is first created. Initializes the UI, loads
     * user information, sets up notification preferences, and prepares
     * the RecyclerView for upcoming events.
     *
     * @param savedInstanceState Bundle containing saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //set up header menu
        UIHelper.setupHeaderAndMenu(this);

        // Initialize Firebase references
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Setup notification toggle
        SwitchCompat notificationSwitch = findViewById(R.id.notifications_switch);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;

        if (userId != null) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean enabled = documentSnapshot.getBoolean("notificationsEnabled");
                            if (enabled != null) notificationSwitch.setChecked(enabled);
                        }
                    });
        }

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (userId != null) {
                db.collection("users").document(userId)
                        .update("notificationsEnabled", isChecked)
                        .addOnFailureListener(e ->
                                Toast.makeText(ProfileActivity.this,
                                        "Failed to update notification preference",
                                        Toast.LENGTH_LONG).show());
            }
        });

        // Setup profile edit and delete buttons

        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            Toast.makeText(this, "Opening profile editor...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProfileEditActivity.class));
        });

        findViewById(R.id.btn_delete_profile).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        Button eventHistoryBtn = findViewById(R.id.btnEventHistory);
        eventHistoryBtn.setOnClickListener(view -> startActivity(new Intent(this, EventHistoryActivity.class)));


        // Display user name and email
        TextView nameView = findViewById(R.id.tv_profile_name);
        TextView emailView = findViewById(R.id.tv_profile_email);
        avatarView = findViewById(R.id.img_avatar);
        setupAvatarPicker();

        if (currentUser != null) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String email = documentSnapshot.getString("email");
                            avatarUrl = documentSnapshot.getString("avatarUrl");
                            nameView.setText((firstName != null && lastName != null) ? firstName + " " + lastName : "Guest User");
                            emailView.setText(email != null ? email : "guest@example.com");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(this).load(avatarUrl).circleCrop().into(avatarView);
                            }
                        } else {
                            nameView.setText("Guest User");
                            emailView.setText("guest@example.com");
                        }
                    })
                    .addOnFailureListener(e -> {
                        nameView.setText("Guest User");
                        emailView.setText("guest@example.com");
                    });
        } else {
            nameView.setText("Guest User");
            emailView.setText("guest@example.com");
        }

        avatarView.setOnClickListener(v -> showAvatarOptions(userId));


        // Load user info for additional refresh
        loadUserInfo();

        // Setup RecyclerView for upcoming events
        RecyclerView rvUpcoming = findViewById(R.id.rv_upcoming_events);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        upcomingEventsAdapter = new UpcomingEventsAdapter(new ArrayList<>(), user);
        rvUpcoming.setAdapter(upcomingEventsAdapter);

        loadUpcomingEvents();  // Only loads data


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo(); // refresh profile whenever page reopens
    }

    /**
     * Loads the user's profile information from Firestore and updates
     * the displayed name and email in the UI.
     */
    private void loadUserInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String firstName = doc.getString("firstName");
                        String lastName = doc.getString("lastName");
                        String email = doc.getString("email");
                        avatarUrl = doc.getString("avatarUrl");

                        ((TextView)findViewById(R.id.tv_profile_name)).setText(firstName + " " + lastName);
                        ((TextView)findViewById(R.id.tv_profile_email)).setText(email);
                        if (avatarUrl != null && !avatarUrl.isEmpty() && avatarView != null) {
                            Glide.with(this).load(avatarUrl).circleCrop().into(avatarView);
                        }
                    }
                });
    }


    /**
     * Deletes the current user's account from Firebase Authentication
     * and removes their data from Firestore. Clears local preferences
     * and redirects the user to the login screen upon success.
     */
    private void deleteAccount() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;

        if (userId == null || auth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: No logged in user.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(v -> auth.getCurrentUser()
                        .delete()
                        .addOnSuccessListener(v2 -> {
                            getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply();
                            Toast.makeText(ProfileActivity.this, "Your account has been deleted.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ProfileActivity.this, LogInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this,
                                "Failed to delete authentication account: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()))
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this,
                        "Failed to delete profile data: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }


    /**
     * Loads the upcoming events from Firestore for the current user
     * and updates the RecyclerView adapter with the new list.
     * Only events in the future are loaded.
     */
    private void loadUpcomingEvents() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Date now = new Date();
        String userId = currentUser.getUid();

        db.collection("events")
                .whereGreaterThan("eventDate", now) // still get future events only
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> userEvents = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Event event = doc.toObject(Event.class);

                        if (event != null && event.getWaitlist() != null) {
                            List<WaitlistEntrant> entrants = event.getWaitlist().getWaitlistEntrants();

                            if (entrants != null) {
                                for (WaitlistEntrant entrant : entrants) {
                                    if (entrant != null && userId.equals(entrant.getUserId())) {

                                        //Add event ONLY if THIS user is part of the waitlist
                                        userEvents.add(event);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    upcomingEventsAdapter.submitList(userEvents);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load upcoming events", Toast.LENGTH_SHORT).show()
                );
    }

    private void setupAvatarPicker() {
        avatarPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            uploadAvatar(uri);
                        }
                    }
                }
        );
    }

    private void showAvatarOptions(String userId) {
        if (userId == null) {
            Toast.makeText(this, "Please log in to update profile photo", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] options = {"Upload Photo", "Remove Photo"};
        new AlertDialog.Builder(this)
                .setTitle("Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openAvatarPicker();
                    } else {
                        removeAvatar(userId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openAvatarPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        avatarPickerLauncher.launch(Intent.createChooser(intent, "Select Profile Photo"));
    }

    private void uploadAvatar(Uri uri) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to update profile photo", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = currentUser.getUid();
        StorageReference ref = FirebaseStorage.getInstance().getReference("avatars/" + uid + ".jpg");
        ref.putFile(uri)
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(download -> {
                    avatarUrl = download.toString();
                    FirebaseFirestore.getInstance().collection("users")
                            .document(uid)
                            .update("avatarUrl", avatarUrl)
                            .addOnSuccessListener(v -> {
                                Glide.with(this).load(avatarUrl).circleCrop().into(avatarView);
                                Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to save photo", Toast.LENGTH_LONG).show());
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Photo upload failed", Toast.LENGTH_LONG).show());
    }

    private void removeAvatar(String userId) {
        avatarUrl = null;
        if (avatarView != null) {
            avatarView.setImageResource(R.drawable.ic_person);
        }
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("avatarUrl", null)
                .addOnSuccessListener(v -> Toast.makeText(this, "Profile photo removed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove photo", Toast.LENGTH_LONG).show());
    }

}
