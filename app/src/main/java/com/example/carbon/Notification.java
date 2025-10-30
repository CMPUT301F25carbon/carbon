package com.example.carbon;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Notification {
    private String userId;
    private String title;
    private String body;
    private long createdAt;
    private boolean read;

    public Notification() {}

    public Notification(String userId, String title, String body) {
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.createdAt = System.currentTimeMillis();
        this.read = false;
    }

    // Firestore writer
    public static Task<Void> notifyUser(@NonNull String userId, @NonNull String title, @NonNull String body) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        var doc = db.collection("notifications").document();
        Map<String,Object> n = new HashMap<>();
        n.put("userId", userId);
        n.put("title", title);
        n.put("body", body);
        n.put("createdAt", System.currentTimeMillis());
        n.put("read", false);
        return doc.set(n);
    }
}

