package com.example.carbon;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * The OrganizerEventDetailActivity is a connecting page that displays some basic details about
 * the event and prompts the organizer to move to various event-specific pages to do some
 * sort of action on the event
 *
 * @author Cooper Goddard
 */
public class OrganizerEventDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE";
    public static final String EXTRA_EVENT_DATE = "EXTRA_EVENT_DATE";     // e.g., "05/12/2025"
    public static final String EXTRA_EVENT_COUNTS = "EXTRA_EVENT_COUNTS"; // e.g., "11 registrations / 5 spots"

    private TextView tvTitle, tvDate, tvDescription;
    private ImageView tvPoster;
    private Button viewWaitlistBtn, viewQRBtn, updatePosterBtn;

    private String eventId;
    private String eventDocId;
    private Event currentEvent;
    private Handler countdownHandler;
    private Runnable countdownRunnable;
    private ActivityResultLauncher<Intent> posterPickerLauncher;
    private Uri pendingPosterUri;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event_details);

        UIHelper.setupHeaderAndMenu(this);

        tvPoster = findViewById(R.id.tv_event_poster);
        tvTitle  = findViewById(R.id.tv_event_title);
        tvDate   = findViewById(R.id.tv_event_date);
        tvDescription = findViewById(R.id.tv_event_description);
        viewWaitlistBtn = findViewById(R.id.view_waitlist_btn);
        viewQRBtn = findViewById(R.id.view_qr_btn);
        updatePosterBtn = findViewById(R.id.update_poster_btn);

        countdownHandler = new Handler(Looper.getMainLooper());
        setupPosterPicker();

        Intent intent = getIntent();
        String eventUuid = intent.getStringExtra("EVENT_ID");
        // Now, use the eventUuid to load your data
        if (eventUuid != null && !eventUuid.isEmpty()) {
            // Your existing logic to load event details from Firestore using the UUID
            loadEventDataFromFirestore(eventUuid);
        } else {
            // Handle the error: no ID was found
            Toast.makeText(this, "Event ID not found.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if there's no data to show
        }

        // Set up onClick listeners for various buttons to different activities
        viewWaitlistBtn.setOnClickListener(v -> {
            Intent newIntent = new Intent(OrganizerEventDetailsActivity.this, EventWaitlistActivity.class);

            // Pass the unique ID of the clicked event to the next activity.
            newIntent.putExtra("EVENT_ID", eventUuid);

            // Start the new activity
            startActivity(newIntent);
        });

        viewQRBtn.setOnClickListener(v -> {
            Intent newIntent = new Intent(OrganizerEventDetailsActivity.this, DisplayQRCodeActivity.class);

            // Pass the unique ID of the clicked event to the next activity.
            newIntent.putExtra("EVENT_UUID", eventUuid);

            // Start the new activity
            startActivity(newIntent);
        });

        updatePosterBtn.setOnClickListener(v -> openPosterPicker());
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
                    OrganizerEventDetailsActivity.this.eventId = event.getUuid();
                    OrganizerEventDetailsActivity.this.eventDocId = document.getId();

                    tvTitle.setText(event.getTitle());
                    if (event.getEventDate() != null) {
                        // Format date and time
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("h a", Locale.US); // e.g., "9 AM", "2 PM"
                        String dateStr = dateFormat.format(event.getEventDate());
                        String timeStr = timeFormat.format(event.getEventDate()).toLowerCase();
                        tvDate.setText(dateStr + " at " + timeStr);

                    }

                    // Display description
                    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                        tvDescription.setText(event.getDescription());
                    } else {
                        tvDescription.setText("No description available.");
                    }

                    String imageUrl = event.getImageURL();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.carbon_start_logo) // Show this while loading
                                .error(R.drawable.ic_delete) // Show this if loading fails
                                .into(tvPoster);
                    } else {
                        // If there's no image URL, you can hide the ImageView or keep the placeholder
                        tvPoster.setImageResource(R.drawable.carbon_start_logo);
                    }

                } else {
                    Toast.makeText(OrganizerEventDetailsActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(OrganizerEventDetailsActivity.this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }

    /**
     * Configures the image picker and upload flow for updating the event poster.
     */
    private void setupPosterPicker() {
        posterPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            pendingPosterUri = uri;
                            Glide.with(this).load(uri).centerCrop().into(tvPoster);
                            uploadPoster(uri);
                        }
                    }
                }
        );
    }

    private void openPosterPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(intent, "Select new poster");
        posterPickerLauncher.launch(chooser);
    }

    private void uploadPoster(Uri uri) {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "event_posters/" + eventId + "-" + UUID.randomUUID() + ".jpg";
        StorageReference ref = FirebaseStorage.getInstance().getReference(fileName);

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String url = downloadUri.toString();
                    updatePosterUrlInEvent(url);
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to get poster URL", Toast.LENGTH_LONG).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Poster upload failed", Toast.LENGTH_LONG).show());
    }

    private void updatePosterUrlInEvent(String url) {
        if (eventDocId == null || eventDocId.isEmpty()) {
            // fallback lookup by uuid
            FirebaseFirestore.getInstance().collection("events")
                    .whereEqualTo("uuid", eventId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (!snap.isEmpty()) {
                            eventDocId = snap.getDocuments().get(0).getId();
                            writePosterUrl(url);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update poster", Toast.LENGTH_LONG).show());
        } else {
            writePosterUrl(url);
        }
    }

    private void writePosterUrl(String url) {
        FirebaseFirestore.getInstance().collection("events")
                .document(eventDocId)
                .update("imageURL", url)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Poster updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save poster", Toast.LENGTH_LONG).show());
    }
}
