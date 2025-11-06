package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use your current splash layout (the one showing the logo)
        setContentView(R.layout.activity_main);

        // If you have auth, decide destination here
        Class<?> next = LogInActivity.class; // or MainActivity if already logged in

        // Delay is optional; set to 0 for instant redirect
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, next));
            finish();
        }, 600);
    }
}
