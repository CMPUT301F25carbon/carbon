package com.example.carbon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model that tracks entrants for an event along with registration window constraints.
 * Outstanding issues: isUserOnWaitlist currently checks object equality and should be revisited
 * if WaitlistEntrant equality semantics change.
 */
public class Waitlist {

    private List<WaitlistEntrant> waitlistEntrants;       // Firebase stores user IDs (not the objects entirely)
    private Date opening;               // When waitlist opens
    private Date deadline;              // When waitlist closes
    private int maxLimit;               // Max number of entrants allowed

    /** Firestore requires a no-argument constructor. */
    public Waitlist() {}

    /**
     * Creates a waitlist with explicit entrant limit.
     * @param opening when signups begin
     * @param deadline when signups close
     * @param maxLimit maximum entrants permitted
     */
    public Waitlist(Date opening, Date deadline, int maxLimit) {
        this.waitlistEntrants = new ArrayList<WaitlistEntrant>();
        this.opening = opening;
        this.deadline = deadline;
        this.maxLimit = maxLimit;
    }

    /**
     * Creates a waitlist with no explicit cap.
     * @param opening when signups begin
     * @param deadline when signups close
     */
    public Waitlist(Date opening, Date deadline) {
        this.waitlistEntrants = new ArrayList<WaitlistEntrant>();
        this.opening = opening;
        this.deadline = deadline;
        this.maxLimit = Integer.MAX_VALUE;
    }

    /**
     * Attempts to join the waitlist within the open/close window and capacity limits.
     * @param userId entrant identifier
     * @return true when the entrant was added; false otherwise
     */
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

    /**
     * Removes a user from the waitlist if present.
     * @param userId entrant identifier
     * @return true when removal succeeds
     */
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

    /**
     * @return number of entrants on the waitlist
     */
    public int getWaitlistCount() {
        return waitlistEntrants.size();
    }

    /**
     * Checks whether a user is already on the waitlist.
     * @param userId entrant identifier
     * @return true if present
     */
    public boolean isUserOnWaitlist(String userId) {
        if (waitlistEntrants == null || userId == null) {
            return false;
        }
        for (WaitlistEntrant entrant : waitlistEntrants) {
            if (entrant != null && userId.equals(entrant.getUserId())) {
                return true;
            }
        }
        return false;
    }

    /** @return entrants persisted for the waitlist */
    public List<WaitlistEntrant> getWaitlistEntrants() { return waitlistEntrants; }
    /** @return opening date */
    public Date getOpening() { return opening; }
    /** @return deadline date */
    public Date getDeadline() { return deadline; }
    /** @return maximum allowed entrants */
    public int getMaxLimit() { return maxLimit; }

}
