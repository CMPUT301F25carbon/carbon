package com.example.carbon;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE";
    public static final String EXTRA_EVENT_DATE = "EXTRA_EVENT_DATE";     // e.g., "05/12/2025"
    public static final String EXTRA_EVENT_COUNTS = "EXTRA_EVENT_COUNTS"; // e.g., "11 registrations / 5 spots"

    private TextView tvTitle, tvDate, tvCounts, tvDescription, tvCountdown;
    private ImageView eventPoster;
    private Button signUpButton;

    private String eventId;
    private String eventDocId;
    private Event currentEvent;
    private List<WaitlistEntrant> currentWaitlistEntrants = new ArrayList<>();
    private Handler countdownHandler;
    private Runnable countdownRunnable;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        UIHelper.setupHeaderAndMenu(this);

        tvTitle  = findViewById(R.id.tv_event_title);
        tvDate   = findViewById(R.id.tv_event_date);
        tvCounts = findViewById(R.id.tv_event_counts);
        tvDescription = findViewById(R.id.tv_event_description);
        tvCountdown = findViewById(R.id.tv_countdown);
        signUpButton = findViewById(R.id.btn_sign_up);
        eventPoster = findViewById(R.id.img_event_poster);
        
        countdownHandler = new Handler(Looper.getMainLooper());

        Intent intent = getIntent();
        String eventUuid = null;

        // Check if the activity was launched by a deep link
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                // The UUID is the last part of the path in "carbondate://events/UUID"
                eventUuid = uri.getLastPathSegment();
            }
        } else {
            // Fallback to the old way (launched from another activity)
            eventUuid = intent.getStringExtra("EXTRA_EVENT_ID");
        }

        // Now, use the eventUuid to load your data
        if (eventUuid != null && !eventUuid.isEmpty()) {
            // Your existing logic to load event details from Firestore using the UUID
            loadEventDataFromFirestore(eventUuid);
        } else {
            // Handle the error: no ID was found
            Toast.makeText(this, "Event ID not found.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if there's no data to show
        }
        String title  = getIntent().getStringExtra(EXTRA_EVENT_TITLE);
        String date   = getIntent().getStringExtra(EXTRA_EVENT_DATE);
        String counts = getIntent().getStringExtra(EXTRA_EVENT_COUNTS);

        tvTitle.setText(title != null ? title : "Event");
        tvDate.setText(date != null ? date : "");
        tvCounts.setText(counts != null ? counts : "");
        
        signUpButton.setOnClickListener(v -> joinWaitlist());
    }

    /**
     * Loads the event based on the passed ID and updates the titles and strings for user visualization
     * @param eventId The ID of the event to view the waitlist of
     *
     * @author Cooper Goddard
     */
    private void loadEventDataFromFirestore(String eventId) {
        Log.d("Event DB", eventId);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference the correct document in the 'events' collection using the eventId
        Query query = db.collection("events").whereEqualTo("uuid", eventId).limit(1);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Check if the query returned any results
                if (!task.getResult().isEmpty()) {
                    // Get the first (and only, otherwise something is very wrong lol) document from the query result
                    DocumentSnapshot document = task.getResult().getDocuments().get(0);

                    // Convert the document into an Event object
                    Event event = document.toObject(Event.class);
                    currentEvent = event;
                    EventDetailsActivity.this.eventId = event.getUuid();
                    EventDetailsActivity.this.eventDocId = document.getId();
                    currentWaitlistEntrants = event != null && event.getWaitlist() != null &&
                            event.getWaitlist().getWaitlistEntrants() != null
                            ? new ArrayList<>(event.getWaitlist().getWaitlistEntrants())
                            : new ArrayList<>();

                    tvTitle.setText(event.getTitle());
                    if (event.getEventDate() != null) {
                        // Format date and time
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("h a", Locale.US); // e.g., "9 AM", "2 PM"
                        String dateStr = dateFormat.format(event.getEventDate());
                        String timeStr = timeFormat.format(event.getEventDate()).toLowerCase();
                        tvDate.setText(dateStr + " at " + timeStr);
                        
                        // Start countdown timer
                        startCountdown(event.getEventDate());
                    }
                    
                    // Display description
                    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                        tvDescription.setText(event.getDescription());
                    } else {
                        tvDescription.setText("No description available.");
                    }
                    String imageUrl = event.getImageURL();
                    if (!TextUtils.isEmpty(imageUrl) && eventPoster != null) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.carbon_start_logo)
                                .centerCrop()
                                .into(eventPoster);
                    } else if (eventPoster != null) {
                        eventPoster.setImageResource(R.drawable.carbon_start_logo);
                    }
                    updateCounts(event, currentWaitlistEntrants);

                } else {
                    Toast.makeText(EventDetailsActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(EventDetailsActivity.this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Updates the counts text using either the event's waitlist or an override list.
     */
    private void updateCounts(Event event) {
        updateCounts(event, currentWaitlistEntrants);
    }

    private void updateCounts(Event event, List<WaitlistEntrant> overrideEntrants) {
        int waitlistCount = 0;
        if (overrideEntrants != null) {
            waitlistCount = overrideEntrants.size();
        } else if (event != null && event.getWaitlist() != null && event.getWaitlist().getWaitlistEntrants() != null) {
            waitlistCount = event.getWaitlist().getWaitlistEntrants().size();
        }
        Integer spots = event != null ? event.getTotalSpots() : null;
        String text = waitlistCount + " on waitlist";
        if (spots != null) {
            text += " / " + spots + " spots";
        }
        tvCounts.setText(text);
    }

    /**
     * Adds the current user to the waitlist in Firestore.
     */
    private void joinWaitlist() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to join the waitlist", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            return;
        }
        if (currentEvent == null || eventDocId == null || eventDocId.isEmpty()) {
            Toast.makeText(this, "Event not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        List<WaitlistEntrant> entrants = new ArrayList<>(currentWaitlistEntrants);

        Integer spots = currentEvent.getTotalSpots();
        if (spots != null && spots > 0 && entrants.size() >= spots * 5) {
            Toast.makeText(this, "Waitlist is full for this event", Toast.LENGTH_LONG).show();
            return;
        }

        for (WaitlistEntrant e : entrants) {
            if (e != null && Objects.equals(e.getUserId(), user.getUid())) {
            Toast.makeText(this, "You are already on the waitlist", Toast.LENGTH_SHORT).show();
            return;
            }
        }

        ensureUserProfile(user, () -> {
            WaitlistEntrant newEntrant = new WaitlistEntrant(user.getUid(), new Date());
            entrants.add(newEntrant);
            currentWaitlistEntrants = entrants;

            FirebaseFirestore.getInstance().collection("events")
                    .document(eventDocId)
                    .update("waitlist.waitlistEntrants", entrants)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Added to waitlist", Toast.LENGTH_SHORT).show();
                        updateCounts(currentEvent, entrants);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to join waitlist", Toast.LENGTH_LONG).show());
        });
    }

    /**
     * Ensures anonymous/device users provide name and email before joining waitlist.
     */
    private void ensureUserProfile(FirebaseUser user, Runnable onReady) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String email = doc.getString("email");
                    boolean needsInfo = TextUtils.isEmpty(first) || TextUtils.isEmpty(last) || TextUtils.isEmpty(email);

                    if (!needsInfo) {
                        onReady.run();
                        return;
                    }
                    showProfilePrompt(user, first, last, email, onReady);
                })
                .addOnFailureListener(e -> {
                    showProfilePrompt(user, null, null, null, onReady);
                });
    }

    private void showProfilePrompt(FirebaseUser user, String first, String last, String email, Runnable onReady) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        android.widget.EditText firstInput = new android.widget.EditText(this);
        firstInput.setHint("First Name");
        if (!TextUtils.isEmpty(first)) firstInput.setText(first);
        layout.addView(firstInput);

        android.widget.EditText lastInput = new android.widget.EditText(this);
        lastInput.setHint("Last Name");
        if (!TextUtils.isEmpty(last)) lastInput.setText(last);
        layout.addView(lastInput);

        android.widget.EditText emailInput = new android.widget.EditText(this);
        emailInput.setHint("Email");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        if (!TextUtils.isEmpty(email)) emailInput.setText(email);
        layout.addView(emailInput);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Enter your details")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String f = firstInput.getText().toString().trim();
                    String l = lastInput.getText().toString().trim();
                    String e = emailInput.getText().toString().trim();
                    if (TextUtils.isEmpty(f) || TextUtils.isEmpty(l) || TextUtils.isEmpty(e)) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseFirestore.getInstance().collection("users")
                            .document(user.getUid())
                            .set(new java.util.HashMap<String, Object>() {{
                                put("firstName", f);
                                put("lastName", l);
                                put("email", e);
                                put("anonymous", false);
                                put("role", "entrant");
                            }}, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(x -> onReady.run())
                            .addOnFailureListener(err -> Toast.makeText(EventDetailsActivity.this, "Failed to save profile", Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Starts the countdown timer for the event
     * @param eventDate The date/time of the event
     */
    private void startCountdown(Date eventDate) {
        if (eventDate == null) {
            tvCountdown.setText("Event date not available");
            return;
        }

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long eventTime = eventDate.getTime();
                long diff = eventTime - now;

                if (diff <= 0) {
                    tvCountdown.setText("Event has started!");
                    return;
                }

                long days = TimeUnit.MILLISECONDS.toDays(diff);
                long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;

                String countdownText;
                if (days > 0) {
                    countdownText = String.format(Locale.US, "%d days, %d hours", days, hours);
                } else if (hours > 0) {
                    countdownText = String.format(Locale.US, "%d hours, %d minutes", hours, minutes);
                } else if (minutes > 0) {
                    countdownText = String.format(Locale.US, "%d minutes, %d seconds", minutes, seconds);
                } else {
                    countdownText = String.format(Locale.US, "%d seconds", seconds);
                }

                tvCountdown.setText(countdownText);
                countdownHandler.postDelayed(this, 1000); // Update every second
            }
        };

        countdownHandler.post(countdownRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }
}
