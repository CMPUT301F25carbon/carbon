package com.example.carbon;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * User registration with Firebase Authentication and Firestore.
 * Includes field validation and a link to the login page.
 */
public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Input fields
        EditText firstNameField = findViewById(R.id.first_name);
        EditText lastNameField = findViewById(R.id.last_name);
        EditText emailField = findViewById(R.id.email);
        EditText password1Field = findViewById(R.id.password1);
        EditText password2Field = findViewById(R.id.password2);
        EditText phoneNoField = findViewById(R.id.phone_no);
        Button signUpButton = findViewById(R.id.sign_up_btn);
        TextView logInText = findViewById(R.id.log_in_text);

        // Sign up button logic
        signUpButton.setOnClickListener(v -> {
            String firstName = firstNameField.getText().toString().trim();
            String lastName = lastNameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password1 = password1Field.getText().toString().trim();
            String password2 = password2Field.getText().toString().trim();
            String phoneNo = phoneNoField.getText().toString().trim();

            // Field validation checks
            if (!validateNotEmpty(Arrays.asList(firstName, lastName, email, password1, password2, phoneNo))) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validateEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validatePhone(phoneNo)) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validatePasswords(password1, password2)) {
                Toast.makeText(this, "Passwords must match and be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // New firebase user
            mAuth.createUserWithEmailAndPassword(email, password1)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            assert mAuth.getCurrentUser() != null;
                            String userId = mAuth.getCurrentUser().getUid();

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("firstName", firstName);
                            userData.put("lastName", lastName);
                            userData.put("email", email);
                            userData.put("phoneNo", phoneNo);
                            userData.put("role", "entrant");
                            userData.put("notificationsEnabled", true);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(userId)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(SignUpActivity.this, "Account created & saved!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(SignUpActivity.this, "Saved Auth but failed Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        } else {
                            Toast.makeText(SignUpActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Login page link
        String text = "Already have an account? Log in here";
        SpannableString spannable = new SpannableString(text);

        int start = text.indexOf("Log in here");
        int end = start + "Log in here".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(SignUpActivity.this, LogInActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(SignUpActivity.this, android.R.color.white));
            }
        };

        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        logInText.setText(spannable);
        logInText.setMovementMethod(LinkMovementMethod.getInstance());
        logInText.setHighlightColor(Color.TRANSPARENT);
    }
    //Validation helper methods

    /** Checks that none of the given strings are empty or null. */
    public boolean validateNotEmpty(List<String> fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }


    /** Validates an email address using Android’s built-in pattern. */
    public boolean validateEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /** Validates that a phone number contains 8–15 digits only. */
    public boolean validatePhone(String phone) {
        return phone.matches("^[0-9]{8,15}$");
    }

    /** Ensures passwords match and are at least 8 characters long. */
    public boolean validatePasswords(String p1, String p2) {
        return p1.length() >= 8 && p1.equals(p2);
    }
}
