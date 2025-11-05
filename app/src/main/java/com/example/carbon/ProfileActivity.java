package com.example.carbon;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UIHelper.setupHeaderAndMenu(this);

        findViewById(R.id.btn_edit_profile).setOnClickListener(v ->
                android.widget.Toast.makeText(this, "Edit profile (todo)", android.widget.Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btn_registrations).setOnClickListener(v ->
                        startActivity(new android.content.Intent(this, NotificationActivity.class))
                // ^ temp: reuse notifications until Registrations screen exists
        );

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
}
