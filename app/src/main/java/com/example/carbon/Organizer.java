package com.example.carbon;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Organizer extends AppCompatActivity {
    private final LotteryService lottery = new LotteryService();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Pass the eventId via Intent when opening Organizer screen
        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing eventId", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        EditText etN = findViewById(R.id.et_sample_n);
        Button btnSample = findViewById(R.id.btn_sample_n);
        Button btnReplacement = findViewById(R.id.btn_draw_replacement);

        btnSample.setOnClickListener(v -> {
            int n = parseIntOr(etN.getText().toString(), 0);
            lottery.sampleNAttendees(eventId, n, new UiCb());
        });

        btnReplacement.setOnClickListener(v -> {
            int n = parseIntOr(etN.getText().toString(), 0);
            lottery.drawReplacement(eventId, n, new UiCb());
        });
    }

    private int parseIntOr(String s, int def) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; } }

    class UiCb implements LotteryService.Callback {
        @Override public void onComplete(int winnersAdded, int remainingCapacity, String info) {
            runOnUiThread(() -> {
                String msg = "Winners: " + winnersAdded + " | Slots left: " + remainingCapacity;
                if (info != null) msg += " (" + info + ")";
                Toast.makeText(Organizer.this, msg, Toast.LENGTH_LONG).show();
            });
        }
        @Override public void onError(Exception e) {
            runOnUiThread(() -> Toast.makeText(Organizer.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}
