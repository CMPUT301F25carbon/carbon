package com.example.carbon;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UIHelper.setupHeaderAndMenu(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SwitchCompat notificationSwitch = findViewById(R.id.notifications_switch);

        String userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("userId", null);

        if (userId != null) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean enabled = documentSnapshot.getBoolean("notificationsEnabled");
                            if (enabled != null) {
                                notificationSwitch.setChecked(enabled);
                            }
                        }
                    });
        }

        notificationSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (userId != null) {
                db.collection("users").document(userId)
                        .update("notificationsEnabled", isChecked)
                        .addOnSuccessListener(v -> {
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(ProfileActivity.this, "Failed to update notifcation preference", Toast.LENGTH_LONG).show());
            }
        }));

        findViewById(R.id.btn_edit_profile).setOnClickListener(v ->
                android.widget.Toast.makeText(this, "Edit profile (todo)", android.widget.Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btn_registrations).setOnClickListener(v ->
                        startActivity(new android.content.Intent(this, NotificationActivity.class))
                // ^ temp: reuse notifications until Registrations screen exists
        );

        findViewById(R.id.btn_delete_profile).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Delete", ((dialog, which) -> deleteAccount()))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Optional: organized events (stub)
        findViewById(R.id.btn_organized_events).setOnClickListener(v ->
                android.widget.Toast.makeText(this, "Organized Events (todo)", android.widget.Toast.LENGTH_SHORT).show()
        );


        // TODO: replace with your real user source (DeviceManager / User singleton / FirebaseAuth)
        TextView name = findViewById(R.id.tv_profile_name);
        TextView email = findViewById(R.id.tv_profile_email);
        name.setText("Guest User");
        email.setText("guest@example.com");
    }


    private void deleteAccount() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("userId", null);

        if (userId == null || auth.getCurrentUser() == null ) {
            Toast.makeText(this, "Error: No logged in user.",Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(v -> {
                    auth.getCurrentUser()
                            .delete()
                            .addOnSuccessListener(v2 -> {
                                getSharedPreferences("user_prefs", MODE_PRIVATE)
                                        .edit()
                                        .clear()
                                        .apply();

                                Toast.makeText(ProfileActivity.this, "Your account has been deleted.", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(ProfileActivity.this, LogInActivity.class);

                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(ProfileActivity.this, "Failed to delete authentication account: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ProfileActivity.this, "Failed to delete profile data: " + e.getMessage(), Toast.LENGTH_LONG).show());

    }
}
