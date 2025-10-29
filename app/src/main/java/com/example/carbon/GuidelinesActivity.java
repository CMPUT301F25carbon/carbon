package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class GuidelinesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guidelines);

        //Get buttons by id
        ImageButton settingsButton = findViewById(R.id.settings_button);
        ImageButton notificationsButton = findViewById(R.id.notification_button);

        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton homeButton = findViewById(R.id.home_button);
        ImageButton profileButton = findViewById(R.id.profile_button);

        //Implement back button
        backButton.setOnClickListener(v -> finish());

        //Implement other buttons (all go to MainActivity for now)
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(GuidelinesActivity.this, MainActivity.class);
            startActivity(intent);
        });

        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(GuidelinesActivity.this, MainActivity.class);
            startActivity(intent);
        });

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(GuidelinesActivity.this, MainActivity.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(GuidelinesActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
