package com.example.carbon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Waitlist {

    private List<WaitlistEntrant> waitlistEntrants;
    private Date opening;
    private Date deadline;

    public Waitlist() {
        this.waitlistEntrants = new ArrayList<>();
    }

    public Waitlist(Date opening, Date deadline) {
        this.waitlistEntrants = new ArrayList<>();
        this.opening = opening;
        this.deadline = deadline;
    }

    // --- Join waitlist ---
    public boolean joinWaitlist(String userId) {
        if (waitlistEntrants == null)
            waitlistEntrants = new ArrayList<>();

        Date now = new Date();
        if (opening != null && now.before(opening)) return false;
        if (deadline != null && now.after(deadline)) return false;

        // Prevent duplicates
        for (WaitlistEntrant entrant : waitlistEntrants) {
            if (entrant.getUserId().equals(userId)) {
                return false;
            }
        }

        waitlistEntrants.add(new WaitlistEntrant(userId, new Date()));
        return true;
    }

    // --- Leave waitlist ---
    public boolean leaveWaitlist(String userId) {
        if (waitlistEntrants == null || waitlistEntrants.isEmpty()) return false;

        for (int i = 0; i < waitlistEntrants.size(); i++) {
            if (waitlistEntrants.get(i).getUserId().equals(userId)) {
                waitlistEntrants.remove(i);
                return true;
            }
        }
        return false;
    }

    // --- Check if user is already on waitlist ---
    public boolean isUserOnWaitlist(String userId) {
        if (waitlistEntrants == null) return false;
        for (WaitlistEntrant entrant : waitlistEntrants) {
            if (entrant.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    // --- Getters & Setters ---
    public List<WaitlistEntrant> getWaitlistEntrants() {
        return waitlistEntrants;
    }

    public void setWaitlistEntrants(List<WaitlistEntrant> waitlistEntrants) {
        this.waitlistEntrants = waitlistEntrants;
    }

    public Date getOpening() {
        return opening;
    }

    public void setOpening(Date opening) {
        this.opening = opening;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }
}
