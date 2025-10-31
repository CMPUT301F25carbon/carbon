package com.example.carbon;

import android.content.Intent;
import android.graphics.Bitmap;import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class DisplayQRCodeActivity extends AppCompatActivity {

    private ImageView qrCodeImageView;
    private Button doneButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_qrcode);

        // Initialize views
        qrCodeImageView = findViewById(R.id.display_qrcode_image_view);
        doneButton = findViewById(R.id.display_qrcode_done_btn);

        // Get the Intent that started this activity
        Intent intent = getIntent();
        String eventUuidString = intent.getStringExtra("EVENT_UUID");

        // Check if the UUID string was passed correctly
        if (eventUuidString != null && !eventUuidString.isEmpty()) {
            // Convert the string back to a UUID object
            UUID eventUuid = UUID.fromString(eventUuidString);

            // Generate the QR code Bitmap
            Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(eventUuid);

            // If generation was successful, display it
            if (qrCodeBitmap != null) {
                qrCodeImageView.setImageBitmap(qrCodeBitmap);
            } else {
                // Handle the error case
                Toast.makeText(this, "Failed to generate QR Code.", Toast.LENGTH_LONG).show();
            }
        } else {
            // Handle the case where the UUID was not passed
            Toast.makeText(this, "Error: Event ID not found.", Toast.LENGTH_LONG).show();
        }

        // Set a listener for the "Done" button to close this activity
        doneButton.setOnClickListener(v -> {
            // Navigate to BrowseEventsActivity (event list)
            Intent newIntent = new Intent(DisplayQRCodeActivity.this, BrowseEventsActivity.class);
            startActivity(newIntent);
            // Finishes the QR code activity and returns to whatever is next in the stack
            finish();
        });
    }
}
