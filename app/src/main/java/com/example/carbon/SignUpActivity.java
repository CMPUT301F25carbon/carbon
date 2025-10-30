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
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        EditText firstNameField = findViewById(R.id.first_name);
        EditText lastNameField = findViewById(R.id.last_name);
        EditText emailField = findViewById(R.id.email);
        EditText password1Field = findViewById(R.id.password1);
        EditText password2Field = findViewById(R.id.password2);
        EditText phoneNoField = findViewById(R.id.phone_no);
        Button signUpButton = findViewById(R.id.sign_up_btn);
        TextView logInText = findViewById(R.id.log_in_text);

        signUpButton.setOnClickListener(v -> {
            String firstName = firstNameField.getText().toString().trim();
            String lastName = lastNameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password1 = password1Field.getText().toString().trim();
            String password2 = password2Field.getText().toString().trim();
            String phoneNo = phoneNoField.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                    password1.isEmpty() || password2.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            } else if (password1.length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            } else if (!password1.equals(password2)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password1)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("firstName", firstName);
                            userData.put("lastName", lastName);
                            userData.put("email", email);
                            userData.put("phoneNo", phoneNo);
                            userData.put("role", "entrant");


                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(userId)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUpActivity.this, "Account created & saved!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignUpActivity.this, "Saved Auth but failed Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

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
}
