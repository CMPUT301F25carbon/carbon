package com.example.carbon;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

/**
 * The waitlistEntrant is the object that is stored within the DB to cut down on the memory used in
 * firebase. Rather than store all the user data again, waitlistEntrant stores the userId, the
 * date of registration, and the status of the user in the waitlist without storing the user name, role, etc.
 *
 * @author Cooper Goddard
 */
public class WaitlistEntrant {
    private String userId;
    private Date registrationDate;
    /**
     * The status flow as such:
     * "Not Selected" --> "Pending" --> "Accepted" || "Denied" || "Expired"
     */
    private String status;
    private Date selectionDate;

    // --- Firestore requires a no-argument constructor ---
    public WaitlistEntrant() {
    }

    // --- Constructor ---
    public WaitlistEntrant(String userId, Date registrationDate) {
        this.userId = userId;
        this.registrationDate = registrationDate;
        this.status = "Not Selected";
    }

    /**
     * Returns the registration date of the waitlistEntrant
     * @return the registration date of the waitlistEntrant
     *
     * @author Cooper Goddard
     */
    public Date getRegistrationDate() {
        return registrationDate;
    }


    /**
     * Returns the userId of the waitlistEntrant
     * @return the userId of the waitlistEntrant
     *
     * @author Cooper Goddard
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the status of the waitlistEntrant
     * @return the string object of the status
     *
     * @author Cooper Goddard
     */
    public String getStatus() {
        return status;
    }

     /**
     * Sets the status of the waitlistEntrant
     * @param status the new status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the date and time the entrant was selected
     * @return selection timestamp or null if not selected yet
     */
    public Date getSelectionDate() {
        return selectionDate;
    }

    /**
     * Sets the selection timestamp for the entrant
     * @param selectionDate time when entrant was chosen
     */
    public void setSelectionDate(Date selectionDate) {
        this.selectionDate = selectionDate;
    }

    /**
     * Interface for creating a callback when fetching the users of the waitlistEntrant,
     * useful for anything that must be done asynchronously (like DB calls)
     *
     * @author Cooper Goddard
     */
    public interface UserCallback {
        void onUserFetched(User user);
        void onError(Exception e);
    }

    /**
     * Based on the userId's within the waitlistEntrant, it fetches the complete user data for those users
     * and returns it through a callback
     * @param callback the callback object through which the user data is passed back
     *
     * @author Cooper Goddard
     */
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
}
