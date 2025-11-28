package com.example.carbon;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.collection.BuildConfig;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class AcceptedListActivity extends AppCompatActivity {

    private Waitlist waitlist;
    private WaitlistAdapter adapter;
    private final ArrayList<WaitlistEntrant> acceptedEntrants = new ArrayList<>();
    private TextView emptyMessage;
    private String eventId;
    private Event currentEvent;

    private final SimpleDateFormat csvDateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_list);

        UIHelper.setupHeaderAndMenu(this);

        Intent intent = getIntent();
        eventId = intent.getStringExtra("EVENT_ID");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_accepted);
        emptyMessage = findViewById(R.id.empty_message_accepted);

        adapter = new WaitlistAdapter(acceptedEntrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        waitlist = new Waitlist();
        loadAcceptedFromDatabase(eventId);

        findViewById(R.id.export_csv_btn).setOnClickListener(v -> exportAcceptedToCsv());
    }

    private void loadAcceptedFromDatabase(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("events")
                .whereEqualTo("uuid", eventId)
                .limit(1);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                    Event event = document.toObject(Event.class);

                    if (event != null && event.getWaitlist() != null) {
                        this.currentEvent = event;
                        this.waitlist = event.getWaitlist();
                        List<WaitlistEntrant> entrants = this.waitlist.getWaitlistEntrants();

                        if (entrants != null && !entrants.isEmpty()) {
                            acceptedEntrants.clear();

                            for (WaitlistEntrant entrant : entrants) {
                                if (entrant != null && Objects.equals(entrant.getStatus(), "Accepted")) {
                                    acceptedEntrants.add(entrant);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            emptyMessage.setVisibility(acceptedEntrants.isEmpty() ? View.VISIBLE : View.GONE);

                            Log.d("Accepted DB", "Loaded " + acceptedEntrants.size() + " accepted entrants.");
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
                Log.e("Accepted DB", "Error loading: ", task.getException());
            }
        });
    }

    private void exportAcceptedToCsv() {
        if (acceptedEntrants.isEmpty()) {
            Toast.makeText(this, "No accepted users to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        int size = acceptedEntrants.size();
        List<String> rows = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            rows.add(""); // placeholder
        }

        AtomicInteger completedCount = new AtomicInteger(0);

        for (int i = 0; i < size; i++) {
            final int index = i;
            WaitlistEntrant entrant = acceptedEntrants.get(i);

            entrant.fetchUserFromDB(new WaitlistEntrant.UserCallback() {
                @Override
                public void onUserFetched(User user) {
                    rows.set(index, buildCsvRow(entrant, user));
                    checkDone();
                }

                @Override
                public void onError(Exception e) {
                    Log.e("CSV_EXPORT", "Failed to fetch user for entrant: " + entrant.getUserId(), e);
                    rows.set(index, buildCsvRow(entrant, null));
                    checkDone();
                }

                private void checkDone() {
                    if (completedCount.incrementAndGet() == size) {
                        writeAndShareCsv(rows);
                    }
                }
            });
        }
    }

    private String buildCsvRow(WaitlistEntrant entrant, User user) {
        String fullName = "";
        String email = "";

        if (user != null) {
            String first = user.getFirstName() != null ? user.getFirstName() : "";
            String last = user.getLastName() != null ? user.getLastName() : "";
            fullName = (first + " " + last).trim();
            if (user.getEmail() != null) {
                email = user.getEmail();
            }
        }

        String timestampStr = "";
        if (entrant.getRegistrationDate() != null) {
            try {
                timestampStr = csvDateFormat.format(entrant.getRegistrationDate());
            } catch (Exception e) {
                Log.w("CSV_EXPORT", "Could not format registration date", e);
            }
        }

        String safeName = safeCsv(fullName);
        String safeEmail = safeCsv(email);

        return safeName + "," + safeEmail + ",\"" + timestampStr + "\"";
    }

    private void writeAndShareCsv(List<String> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name,Email,Timestamp\n");
        for (String row : rows) {
            sb.append(row).append("\n");
        }

        try {
            File externalFilesDir = getExternalFilesDir(null);
            if (externalFilesDir == null) {
                Toast.makeText(this, "Cannot access external storage.", Toast.LENGTH_SHORT).show();
                return;
            }

            File exportDir = new File(externalFilesDir, "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File csvFile = new File(exportDir, "accepted_list_" + eventId + ".csv");

            FileOutputStream fos = new FileOutputStream(csvFile);
            fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    csvFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Export attendee list"));

        } catch (Exception e) {
            Log.e("CSV_EXPORT", "Error exporting CSV", e);
            Toast.makeText(this, "Failed to export CSV.", Toast.LENGTH_SHORT).show();
        }
    }

    private String safeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
