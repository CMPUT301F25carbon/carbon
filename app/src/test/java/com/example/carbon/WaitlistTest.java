package com.example.carbon;

import java.util.*;

/**
 * Manual tests for the Waitlist class.
 * Each test prints PASS/FAIL to the console.
 */
public class WaitlistTest {

    private Waitlist waitlist;
    private Date now;
    private Date open;
    private Date close;

    public static void main(String[] args) {
        WaitlistTest tester = new WaitlistTest();
        tester.runAllTests();
    }

    void runAllTests() {
        setUp(); testEmptyWaitlistInitially();
        setUp(); testJoinWaitlistWithinTime();
        setUp(); testJoinWaitlistBeforeOpening();
        setUp(); testJoinWaitlistAfterDeadline();
        setUp(); testDuplicateUserCannotJoinTwice();
        setUp(); testLeaveWaitlistSuccessfully();
        setUp(); testLeaveWaitlistUserNotFound();
        setUp(); testIsUserOnWaitlist();
    }

    void setUp() {
        now = new Date();
        open = new Date(now.getTime() - 1000 * 60 * 60);   // opened 1 hour ago
        close = new Date(now.getTime() + 1000 * 60 * 60);  // closes in 1 hour
        waitlist = new Waitlist(open, close);
    }

    // ---------- Helper assertions ----------
    private void assertTrue(boolean condition, String message) {
        if (condition) System.out.println("✅ PASS: " + message);
        else System.out.println("❌ FAIL: " + message);
    }

    private void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if (Objects.equals(expected, actual)) {
            System.out.println("✅ PASS: " + message);
        } else {
            System.out.println("❌ FAIL: " + message +
                    " (expected: " + expected + ", got: " + actual + ")");
        }
    }

    // ---------- Tests ----------

    void testEmptyWaitlistInitially() {
        assertTrue(waitlist.getWaitlistEntrants().isEmpty(),
                "Waitlist should be empty initially");
    }

    void testJoinWaitlistWithinTime() {
        boolean result = waitlist.joinWaitlist("Alice");
        assertTrue(result, "User can join within open period");

        // assuming WaitlistEntrant has getUserId()
        boolean containsAlice = waitlist.getWaitlistEntrants()
                .stream().anyMatch(e -> e.getUserId().equals("Alice"));
        assertTrue(containsAlice, "Waitlist should contain Alice after joining");
    }

    void testJoinWaitlistBeforeOpening() {
        Date futureOpen = new Date(now.getTime() + 1000 * 60 * 60);  // opens in 1 hour
        Date futureClose = new Date(now.getTime() + 1000 * 60 * 120); // closes in 2 hours
        Waitlist futureWaitlist = new Waitlist(futureOpen, futureClose);
        boolean result = futureWaitlist.joinWaitlist("Bob");
        assertFalse(result, "User cannot join before opening time");
    }

    void testJoinWaitlistAfterDeadline() {
        Date pastOpen = new Date(now.getTime() - 1000 * 60 * 120); // opened 2 hours ago
        Date pastClose = new Date(now.getTime() - 1000 * 60 * 60); // closed 1 hour ago
        Waitlist expiredWaitlist = new Waitlist(pastOpen, pastClose);
        boolean result = expiredWaitlist.joinWaitlist("Charlie");
        assertFalse(result, "User cannot join after the deadline");
    }

    void testDuplicateUserCannotJoinTwice() {
        waitlist.joinWaitlist("Diana");
        boolean secondJoin = waitlist.joinWaitlist("Diana");
        assertFalse(secondJoin, "Duplicate user cannot join twice");
        assertEquals(1, waitlist.getWaitlistEntrants().size(),
                "Waitlist should contain only one instance of Diana");
    }

    void testLeaveWaitlistSuccessfully() {
        waitlist.joinWaitlist("Eve");
        boolean left = waitlist.leaveWaitlist("Eve");
        assertTrue(left, "User should be able to leave waitlist successfully");

        boolean stillInList = waitlist.getWaitlistEntrants()
                .stream().anyMatch(e -> e.getUserId().equals("Eve"));
        assertFalse(stillInList, "Eve should be removed after leaving");
    }

    void testLeaveWaitlistUserNotFound() {
        boolean left = waitlist.leaveWaitlist("Frank");
        assertFalse(left, "Cannot leave waitlist if user not found");
    }

    void testIsUserOnWaitlist() {
        waitlist.joinWaitlist("Grace");
        assertTrue(waitlist.isUserOnWaitlist("Grace"),
                "Grace should be on the waitlist after joining");
        assertFalse(waitlist.isUserOnWaitlist("Henry"),
                "Henry should not be on the waitlist");
    }
}
