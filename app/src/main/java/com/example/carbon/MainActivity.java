package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(R.id.start_screen);
        rootView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GuidelinesActivity.class);
            startActivity(intent);
        });
    }
}
