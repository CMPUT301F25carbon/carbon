package com.example.carbon;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class WaitlistEntrant {
    private String userId;
    private Date registrationDate;
    private boolean selected;

    // --- Firestore requires a no-argument constructor ---
    public WaitlistEntrant() {
    }

    // --- Constructor ---
    public WaitlistEntrant(String userId, Date registrationDate) {
        this.userId = userId;
        this.registrationDate = registrationDate;
        this.selected = false;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }


    public String getUserId() {
        return userId;
    }

    public boolean isSelected() {
        return selected;
    }

    public interface UserCallback {
        void onUserFetched(User user);
        void onError(Exception e);
    }

    public void fetchUserFromDB(UserCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError(new Exception("User ID is null or empty."));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Data is ready! Send the User object back via the callback.
                            User user = document.toObject(User.class);
                            callback.onUserFetched(user);
                        } else {
                            // Handle the case where the user document doesn't exist.
                            callback.onError(new Exception("User document not found."));
                        }
                    } else {
                        // Handle the case where the query failed.
                        callback.onError(task.getException());
                    }
                });
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WaitlistEntrant)) return false;
        WaitlistEntrant other = (WaitlistEntrant) obj;
        return userId != null && userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }

}