package com.example.carbon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Waitlist {
    private List<String> userIds;
    private Date deadline;
    private Date opening;

    // Firestore requires a no-argument constructor
    public Waitlist() {}

    public Waitlist(Date deadline, Date opening) {
        // Initialize the list
        this.userIds = new ArrayList<>();
        this.deadline = deadline;
        this.opening = opening;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public Date getDeadline() {
        return deadline;
    }

    public Date getOpening() {
        return opening;
    }


    /**
     * Adds a user's ID to the waitlist if the registration period is active.
     * @param userId The unique ID of the user to add.
     */
    public void addUser(String userId) {
        Date currentDate = new Date();
        if (currentDate.after(opening) && currentDate.before(deadline)) {
            if (!userIds.contains(userId)) { // Prevent duplicate entries
                userIds.add(userId);
            }
        } else {
            throw new IllegalStateException("Registration period is not currently in effect.");
        }
    }
}
