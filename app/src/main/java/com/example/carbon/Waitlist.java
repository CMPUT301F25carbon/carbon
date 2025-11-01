package com.example.carbon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Waitlist {

    private List<String> userIds;       // Firebase stores user IDs (not the objects entirely)
    private Date opening;               // When waitlist opens
    private Date deadline;              // When waitlist closes
    //private int maxLimit;               // Max number of entrants allowed: if we need it!!!

    // --- Firestore requires a no-argument constructor ---
    public Waitlist() {}

    // --- Constructor ---
    public Waitlist(Date opening, Date deadline){ //, int maxLimit) {
        this.userIds = new ArrayList<>();
        this.opening = opening;
        this.deadline = deadline;
        //this.maxLimit = maxLimit; Do we need a limit for the waitlist? ---Ask TA
    }

    // --- Join waitlist ---
    public boolean joinWaitlist(String userId) {
        Date now = new Date();

        if (now.before(opening) || now.after(deadline)) {
            System.out.println("Waitlist not open for registration.");
            return false;
        }

    //       if (userIds.size() >= maxLimit) {
    //        System.out.println("Waitlist is full for event: " + eventId);
      //      return false;
        //}


        if (userIds.contains(userId)) {
            System.out.println("User already on waitlist.");
            return false;
        }

        userIds.add(userId);
        System.out.println("User " + userId + " joined the waitlist.");
        return true;
    }

    // --- Leave waitlist ---
    public boolean leaveWaitlist(String userId) {
        if (userIds.remove(userId)) {
            System.out.println("User " + userId + " left the waitlist.");
            return true;
        } else {
            System.out.println("User not found on waitlist.");
            return false;
        }
    }

    // --- Get total count ---
    public int getWaitlistCount() {
        return userIds.size();
    }

    // --- Check if a user is on waitlist ---
    public boolean isUserOnWaitlist(String userId) {
        return userIds.contains(userId);
    }

    // --- Getters for Firebase serialization ---
    public List<String> getUserIds() { return userIds; }
    public Date getOpening() { return opening; }
    public Date getDeadline() { return deadline; }
    //public int getMaxLimit() { return maxLimit; }

    // --- Optional: Setter if needed by Firestore ---
    public void setUserIds(List<String> userIds) { this.userIds = userIds; }
    public void setOpening(Date opening) { this.opening = opening; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    //public void setMaxLimit(int maxLimit) { this.maxLimit = maxLimit; }
}
