package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileEditActivity extends AppCompatActivity {

    EditText etFirstName, etLastName, etEmail, etPhone;

    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // --- Main form views ---
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);

        btnSave = findViewById(R.id.btn_save);

        // --- Bottom menu buttons ---
        ImageButton btnBack = findViewById(R.id.back_button);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish()); // Close activity
        }

        ImageButton btnHome = findViewById(R.id.home_button);
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        ImageButton btnProfile = findViewById(R.id.profile_button);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        // --- Save button ---
        btnSave.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "First name, last name, and email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(user.getUid())
                    .update(
                            "firstName", firstName,
                            "lastName", lastName,
                            "email", email,
                            "phone", phone
                    )
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

    }

}
