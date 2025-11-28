package com.example.carbon;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
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
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LogInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        EditText emailField = findViewById(R.id.email);
        EditText passwordField = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.sign_up_btn);
        TextView signUpText = findViewById(R.id.sign_up_text);
        Button deviceLogin = findViewById(R.id.device_login_btn);

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            db.collection("users").document(userId).get()
                                    .addOnSuccessListener(document -> {
                                        if (document.exists()) {
                                            String role = document.getString("role");
                                            saveUserToCache(userId, role);
                                            if (Objects.equals(role, "organizer")) {
                                                startActivity(new Intent(LogInActivity.this, BrowseOrganizerEventsActivity.class));
                                            }
                                            else {
                                                startActivity(new Intent(LogInActivity.this, BrowseEventsActivity.class));
                                            }
                                            finish();
                                        } else {
                                            Toast.makeText(LogInActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(LogInActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                    );
                        } else {
                            Toast.makeText(LogInActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        });


        deviceLogin.setOnClickListener(v -> {
            Log.d("DEVICE_LOGIN", "Device login button pressed");

            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null) {
                Log.d("DEVICE_LOGIN", "User already authenticated: " + auth.getCurrentUser().getUid());
                startActivity(new Intent(LogInActivity.this, BrowseEventsActivity.class));
                finish();
                return;
            }

            Log.d("DEVICE_LOGIN", "Attempting anonymous sign-in...");

            auth.signInAnonymously().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e("DEVICE_LOGIN", "Anonymous sign-in FAILED", task.getException());
                    Toast.makeText(this, "Failed to continue with device ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uid = auth.getCurrentUser().getUid();
                Log.d("DEVICE_LOGIN", "Anonymous sign-in SUCCESS. UID: " + uid);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Map<String, Object> data = new HashMap<>();
                data.put("role", "entrant");
                data.put("anonymous", true);
                data.put("notificationsEnabled", true);

                Log.d("DEVICE_LOGIN", "Writing user document to Firestore...");

                db.collection("users").document(uid)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener(x -> {
                            Log.d("DEVICE_LOGIN", "Firestore write SUCCESS for UID: " + uid);

                            if (!hasShownPrivacyNotice()) {
                                Log.d("DEVICE_LOGIN", "Privacy notice has NOT been shown. Displaying dialog...");
                                showPrivacyDialog(() -> launchBrowse());
                            } else {
                                Log.d("DEVICE_LOGIN", "Privacy notice already shown. Launching browse...");
                                launchBrowse();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("DEVICE_LOGIN", "Firestore write FAILED: " + e.getMessage(), e);
                        });
            });
        });

        String text = "Don't have an account? Sign up here";
        SpannableString spannable = new SpannableString(text);

        int start = text.indexOf("Sign up here");
        int end = start + "Sign up here".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(LogInActivity.this, SignUpActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(LogInActivity.this, android.R.color.white));
            }
        };

        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        signUpText.setText(spannable);
        signUpText.setMovementMethod(LinkMovementMethod.getInstance());
        signUpText.setHighlightColor(Color.TRANSPARENT);
    }

    private void saveUserToCache(String userId, String role) {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putString("userId", userId)
                .putString("role", role)
                .apply();
    }

    private boolean hasShownPrivacyNotice() {
        return getSharedPreferences("prefs", MODE_PRIVATE)
                .getBoolean("privacy_notice_shown", false);
    }

    private void setPrivacyNoticeShown() {
        getSharedPreferences("prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("privacy_notice_shown", true)
                .apply();
    }

    private void showPrivacyDialog(Runnable onContinue) {
        Log.d("PRIVACY_DIALOG", "Opening privacy dialog");

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.privacy_notice_dialog);

        Button ok = dialog.findViewById(R.id.ok_btn);
        ok.setOnClickListener(v -> {
            Log.d("PRIVACY_DIALOG", "User pressed OK");
            dialog.dismiss();
            setPrivacyNoticeShown();
            onContinue.run();
        });

        dialog.show();
    }

    private void launchBrowse() {
        startActivity(new Intent(this, BrowseEventsActivity.class));
        finish();
    }

}
