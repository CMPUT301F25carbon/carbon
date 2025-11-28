package com.example.carbon;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.UUID;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The CreateEventActivity holds the logic for the activity_create_event.xml page.
 * Given that an organizer is logged in and viewing this page, they are able to create an event
 * under their ownership with a variety of params.
 *
 * @author Cooper Goddard
 */
public class CreateEventActivity extends AppCompatActivity {

    // Declare all view variables
    private EditText eventTitleInput, eventDesInput, eventDateInput, eventAddressInput,
            eventCityInput, eventProvinceInput, eventCountryInput, eventRegistrationOpeningInput,
            eventRegistrationDeadlineInput, eventSeatInput, eventWaitlistCapacity;
    private Spinner eventCategorySpinner;
    private Button createEventButton;

    // Declare variables needed for the image upload
    private ImageView eventPosterImageView;
    private Uri imageUri; // this is the app uri
    private String imageUrl; // this is the DB url
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // Calendar instance for the date pickers
    private final Calendar myCalendar = Calendar.getInstance();

    // Define the date format for consistency
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        // Setup header and footer
        UIHelper.setupHeaderAndMenu(this);

        UIHelper.setupHeaderAndMenu(this);

        // Initialize all views from the layout
        setupViews();

        // Attach the date picker functionality to the date input fields
        setupDatePicker(eventDateInput);
        setupDatePicker(eventRegistrationDeadlineInput);
        setupDatePicker(eventRegistrationOpeningInput);

        // Set home button in bottom menu
        ImageButton homeButton = findViewById(R.id.home_button);
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> UIHelper.navigateHome(this));
        }

        // Set up listener and launcher for image upload box click
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        eventPosterImageView.setImageURI(imageUri); // Show the uploaded image
                        eventPosterImageView.setPadding(0,0,0,0); // Remove padding after image is set
                    }
                });
        eventPosterImageView.setOnClickListener(v -> openImagePicker());

        // Set up create event button listener
        createEventButton.setOnClickListener(v -> {
            if (validateInput()) {
                uploadImage();
                // Automatically moves into create event on image upload success
            }
        });

    }

    /**
     * This function uploads the user provided image into the storage section of the DB for
     * use in the event object later in creation.
     */
    private void uploadImage() {
        // If no image is selected, proceed without uploading
        if (imageUri == null) {
            imageUrl = null;
            createEvent();
            return;
        }

        Snackbar.make(findViewById(R.id.create_event_root), "Uploading image...", Snackbar.LENGTH_INDEFINITE).show();

        // Create a reference in Firebase Storage
        // The path will be "event_posters/{event_uuid}.jpg"
        String fileName = UUID.randomUUID().toString() + ".jpg";
        com.google.firebase.storage.StorageReference storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReference("event_posters/" + fileName);

        // Upload to storage in FB
        com.google.firebase.storage.UploadTask uploadTask = storageRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Image uploaded successfully, now get the download URL
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                imageUrl = uri.toString();
                createEvent();
            }).addOnFailureListener(e -> {
                // Failed to get download URL
                Snackbar.make(findViewById(R.id.create_event_root), "Failed to get image URL.", Snackbar.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {
            // Handle unsuccessful uploads
            Snackbar.make(findViewById(R.id.create_event_root), "Image upload failed.", Snackbar.LENGTH_LONG).show();
        });
    }

    /**
     * Initializes all the EditText and Button views from the XML layout.
     */
    private void setupViews() {
        eventTitleInput = findViewById(R.id.create_event_title_input);
        eventDesInput = findViewById(R.id.create_event_des_input);
        eventDateInput = findViewById(R.id.create_event_date_input);
        eventAddressInput = findViewById(R.id.create_event_address_input);
        eventCityInput = findViewById(R.id.create_event_city_input);
        eventProvinceInput = findViewById(R.id.create_event_province_input);
        eventCountryInput = findViewById(R.id.create_event_country_input);
        eventRegistrationOpeningInput = findViewById(R.id.create_event_registration_opening_input);
        eventRegistrationDeadlineInput = findViewById(R.id.create_event_registration_deadline_input);
        eventSeatInput = findViewById(R.id.create_event_seats_input);
        createEventButton = findViewById(R.id.create_event_btn);
        eventWaitlistCapacity = findViewById(R.id.create_event_max_waitlist_input);
        eventPosterImageView = findViewById(R.id.upload_event_poster);
        eventCategorySpinner = findViewById(R.id.create_event_category_spinner);

        // Populate categories with a required selection
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Select Category");
        categories.addAll(Arrays.asList(getResources().getStringArray(R.array.event_categories)));

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventCategorySpinner.setAdapter(categoryAdapter);
        eventCategorySpinner.setSelection(0);
    }

    /**
     * Uses the built-in android file system to allow the user to upload the image into the app
     * without having to push for larger permissions from the device.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        Intent chooser = Intent.createChooser(intent, "Select Event Poster");
        imagePickerLauncher.launch(chooser);
    }

    /**
     * Attaches a DatePickerDialog to an EditText's click listener.
     * @param editText The EditText that will trigger the dialog.
     */
    private void setupDatePicker(final EditText editText) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            // Update the EditText with the selected date in the correct format
            editText.setText(sdf.format(myCalendar.getTime()));
        };

        // When the EditText is clicked, show the DatePickerDialog
        editText.setOnClickListener(v -> {
            // Use a new calendar instance to avoid carrying over dates between pickers
            Calendar currentCalendar = Calendar.getInstance();
            new DatePickerDialog(CreateEventActivity.this, dateSetListener,
                    currentCalendar.get(Calendar.YEAR),
                    currentCalendar.get(Calendar.MONTH),
                    currentCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    /**
     * Validates all required user inputs before creating an event.
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateInput() {
        // --- TEXT FIELD VALIDATION ---
        if (TextUtils.isEmpty(eventTitleInput.getText().toString().trim())) {
            eventTitleInput.setError("Event Title is required");
            eventTitleInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(eventDesInput.getText().toString().trim())) {
            eventDesInput.setError("Event Description is required");
            eventDesInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(eventDateInput.getText().toString().trim())) {
            eventDateInput.setError("Event Date is required");
            eventDateInput.requestFocus();
            eventDateInput.performClick();
            return false;
        }

        if (TextUtils.isEmpty(eventAddressInput.getText().toString().trim())) {
            eventAddressInput.setError("Event Address is required");
            eventAddressInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(eventCityInput.getText().toString().trim())) {
            eventCityInput.setError("Event City is required");
            eventCityInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(eventProvinceInput.getText().toString().trim())) {
            eventProvinceInput.setError("Event Province is required");
            eventProvinceInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(eventCountryInput.getText().toString().trim())) {
            eventCountryInput.setError("Event Country is required");
            eventCountryInput.requestFocus();
            return false;
        }

        String selectedCategory = (String) eventCategorySpinner.getSelectedItem();
        if (selectedCategory == null || "Select Category".equals(selectedCategory)) {
            Snackbar.make(findViewById(R.id.create_event_root), "Please select a category", Snackbar.LENGTH_SHORT).show();
            eventCategorySpinner.performClick();
            return false;
        }

        // --- NUMBER FIELD VALIDATION ---
        if (TextUtils.isEmpty(eventSeatInput.getText().toString().trim())) {
            eventSeatInput.setError("Number of Seats is required");
            eventSeatInput.requestFocus();
            return false;
        }
        try {
            Integer.parseInt(eventSeatInput.getText().toString().trim());
        } catch (NumberFormatException e) {
            eventSeatInput.setError("Please enter a valid number");
            eventSeatInput.requestFocus();
            return false;
        }

        // --- COMPLEX DATE VALIDATION (Example) ---
        try {
            Date eventDate = sdf.parse(eventDateInput.getText().toString());
            Date today = new Date(); // Gets current time

            // prevent users from selecting a past date for the event
            if (eventDate.before(today)) {
                eventDateInput.setError("Event date cannot be in the past");
                eventDateInput.requestFocus();
                return false;
            }

            String deadlineString = eventRegistrationDeadlineInput.getText().toString().trim();
            if (!deadlineString.isEmpty()) {
                Date deadlineDate = sdf.parse(deadlineString);
                if (deadlineDate.after(eventDate)) {
                    eventRegistrationDeadlineInput.setError("Deadline cannot be after the event date");
                    eventRegistrationDeadlineInput.requestFocus();
                    return false;
                }
            }
        } catch (ParseException e) {
            eventDateInput.setError("Invalid date format");
            eventDateInput.requestFocus();
            return false;
        }

        return true; // All validation passed
    }

    /**
     * Gathers data, converts types, creates an Event object, and provides user feedback, sends to QR code page if complete.
     */
    private void createEvent() {
        // GET THE CURRENT LOGGED-IN USER
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // No user is logged in, do not proceed.
            Snackbar.make(findViewById(R.id.create_event_root), "You must be logged in to create an event.", Snackbar.LENGTH_LONG).show();
            // redirect to the login screen.
            startActivity(new Intent(CreateEventActivity.this, LogInActivity.class));
            return;
        }
        String ownerId = currentUser.getUid(); // Get the user's unique ID

        // GATHER STRING DATA
        String title = eventTitleInput.getText().toString().trim();
        String des = eventDesInput.getText().toString().trim();
        String address = eventAddressInput.getText().toString().trim();
        String city = eventCityInput.getText().toString().trim();
        String province = eventProvinceInput.getText().toString().trim();
        String country = eventCountryInput.getText().toString().trim();
        String category = (String) eventCategorySpinner.getSelectedItem();
        String waitlistCapacity = eventWaitlistCapacity.getText().toString().trim();

        // CONVERT TO CORRECT DATA TYPES
        int seats = Integer.parseInt(eventSeatInput.getText().toString().trim());
        Date eventDate = null;
        Date deadlineDate = null;
        Date openingDate = null;

        try {
            // Parse the event date string into a Date object
            eventDate = sdf.parse(eventDateInput.getText().toString());

            // Parse the optional deadline string into a Date object
            String deadlineString = eventRegistrationDeadlineInput.getText().toString();
            if (!deadlineString.isEmpty()) {
                deadlineDate = sdf.parse(deadlineString);
            }

            // Parse the optional opening string into a Date object
            String openingString = eventRegistrationOpeningInput.getText().toString();
            if (!openingString.isEmpty()) {
                openingDate = sdf.parse(openingString);
            }
        } catch (ParseException e) {
            // This is a final safeguard. It's unlikely to be triggered.
            Snackbar.make(findViewById(R.id.create_event_root), "An unexpected error occurred with the date format.", Snackbar.LENGTH_SHORT).show();
            return; // Stop the event creation process
        }
        if (deadlineDate == null) {
            deadlineDate = eventDate; // If no deadline is provided, set it to the event date
        }
        if (openingDate == null) {
            openingDate = new Date(); // If no opening is provided, set it to now
        }

        // CREATE FIRESTORE INSTANCE AND GENERATE EVENT ID FIRST
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new document reference so we can get its ID before saving
        String eventId = db.collection("events").document().getId();

        // CREATE THE WAITLIST
        Waitlist newWaitlist;
        if (waitlistCapacity.isEmpty()){
            newWaitlist = new Waitlist(openingDate, deadlineDate);
        } else {
            newWaitlist = new Waitlist(openingDate, deadlineDate, Integer.parseInt(waitlistCapacity));
        }

        newWaitlist.joinWaitlist("1qfLOhloVxPiT80wmC0sMpVt7ko2"); // TODO Remove this later. Just for testing.
        newWaitlist.joinWaitlist("AgpSxi4xl1hgiZLRaYvuLRnPew03");

        // CREATE THE EVENT OBJECT AND INCLUDE THE WAITLIST
        Event newEvent = new Event(title, des, seats, eventDate, address, city, province, country, ownerId, newWaitlist, imageUrl);
        newEvent.setCategory(category);

        // PROVIDE FEEDBACK AND PROCEED
        db.collection("events")
                .add(newEvent)
                .addOnSuccessListener(documentReference -> {
                    Snackbar.make(findViewById(R.id.create_event_root), "Event created successfully!", Snackbar.LENGTH_LONG).show();

                    // Get the UUID from the event we just created
                    String eventUuidString = newEvent.getUuid();

                    // Create an Intent to start DisplayQRCodeActivity
                    Intent intent = new Intent(CreateEventActivity.this, DisplayQRCodeActivity.class);

                    // Pass the event's UUID to the new activity
                    intent.putExtra("EVENT_UUID", eventUuidString);

                    // Start the new activity
                    startActivity(intent);

                    // Finish this activity so the user can't come back to the form
                    finish();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(findViewById(R.id.create_event_root), "Error: Could not create event.", Snackbar.LENGTH_LONG).show();
                });
    }
}
