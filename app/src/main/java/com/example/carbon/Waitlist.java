package com.example.carbon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Waitlist {

    private List<WaitlistEntrant> waitlistEntrants;       // Firebase stores user IDs (not the objects entirely)
    private Date opening;               // When waitlist opens
    private Date deadline;              // When waitlist closes
    private int maxLimit;               // Max number of entrants allowed

    // --- Firestore requires a no-argument constructor ---
    public Waitlist() {}

    // --- Constructor ---
    public Waitlist(Date opening, Date deadline, int maxLimit) {
        this.waitlistEntrants = new ArrayList<WaitlistEntrant>();
        this.opening = opening;
        this.deadline = deadline;
        this.maxLimit = maxLimit;
    }

    public Waitlist(Date opening, Date deadline) {
        this.waitlistEntrants = new ArrayList<WaitlistEntrant>();
        this.opening = opening;
        this.deadline = deadline;
        this.maxLimit = Integer.MAX_VALUE;
    }

    // --- Join waitlist ---
    public boolean joinWaitlist(String userId) {
        Date now = new Date();

        if (now.before(opening) || now.after(deadline)) {
            System.out.println("Waitlist not open for registration.");
            return false;
        }

           if (waitlistEntrants.size() >= maxLimit) {
            System.out.println("Waitlist is full for event");
            return false;
        }

        WaitlistEntrant newWaitlistEntrant = new WaitlistEntrant(userId, new Date());
           // look for userId already within the waitlist
        for (WaitlistEntrant waitlistEntrant : waitlistEntrants) {
            if (waitlistEntrant.getUserId().equals(userId)) {
                System.out.println("User already on waitlist.");
                return false;
            }
        }

        waitlistEntrants.add(newWaitlistEntrant);
        System.out.println("User " + userId + " joined the waitlist.");
        return true;
    }

    // --- Leave waitlist ---
    public boolean leaveWaitlist(String userId) {
        for (WaitlistEntrant waitlistEntrant : waitlistEntrants) {
            if (waitlistEntrant.getUserId().equals(userId)) {
                waitlistEntrants.remove(waitlistEntrant);
                System.out.println("User " + userId + " left the waitlist.");
                return true;
            }
        }
        System.out.println("User not found on waitlist.");
        return false;
    }

    // --- Get total count ---
    public int getWaitlistCount() {
        return waitlistEntrants.size();
    }

    // --- Check if a user is on waitlist ---
    public boolean isUserOnWaitlist(String userId) {
        return waitlistEntrants.contains(userId);
    }

    // --- Getters for Firebase serialization ---
    public List<WaitlistEntrant> getWaitlistEntrants() { return waitlistEntrants; }
    public Date getOpening() { return opening; }
    public Date getDeadline() { return deadline; }
    public int getMaxLimit() { return maxLimit; }

}
