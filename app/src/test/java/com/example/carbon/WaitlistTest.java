package com.example.carbon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Waitlist class.
 * 
 * to verify correct behavior for:
 *  - Joining/leaving the waitlist
 *  - Preventing duplicates
 *  - Handling open/close date constraints?????
 *  - Getters and setters (Firestore compatibility)
 */ 

//Comment for self: Think about max limit tests for the waitlist, and firebase tests??

public class WaitlistTest {

    private Waitlist waitlist;
    private Date now;
    private Date open;
    private Date close;

    /**
     * Runs before each test.
     * Creates a waitlist that opened 1 hour ago and closes 1 hour from now.
     */
    @BeforeEach
    void setUp() {
        now = new Date();
        open = new Date(now.getTime() - 1000 * 60 * 60);   // opened 1 hour ago
        close = new Date(now.getTime() + 1000 * 60 * 60);  // closes in 1 hour
        waitlist = new Waitlist("event123", open, close);
    }

    /**
     * Test: User successfully joins when the waitlist is open.
     */
    @Test
    void testJoinWaitlistWithinTime() {
        boolean joined = waitlist.joinWaitlist("user1");

        assertTrue(joined, "User should be able to join when within open period.");
        assertEquals(1, waitlist.getWaitlistCount(), "Waitlist count should increase.");
        assertTrue(waitlist.isUserOnWaitlist("user1"), "User should be recorded as joined.");
    }

    /**
     * Failed? Test: User cannot join before the waitlist opens.
     */
    @Test
    void testJoinWaitlistBeforeOpening() {
        Date futureOpening = new Date(now.getTime() + 1000 * 60 * 60);  // opens in 1 hour
        Date laterDeadline = new Date(now.getTime() + 1000 * 60 * 120); // closes in 2 hours
        Waitlist futureWaitlist = new Waitlist("eventFuture", futureOpening, laterDeadline);

        boolean joined = futureWaitlist.joinWaitlist("user1");

        assertFalse(joined, "Joining should fail before opening time.");
        assertEquals(0, futureWaitlist.getWaitlistCount(), "No users should be added.");
    }

    /**
     * failed Test: User cannot join after the waitlist deadline has passed.
     */
    @Test
    void testJoinWaitlistAfterDeadline() {
        Date pastOpening = new Date(now.getTime() - 1000 * 60 * 120); // opened 2 hours ago
        Date pastDeadline = new Date(now.getTime() - 1000 * 60 * 60); // closed 1 hour ago
        Waitlist expiredWaitlist = new Waitlist("eventPast", pastOpening, pastDeadline);

        boolean joined = expiredWaitlist.joinWaitlist("user1");

        assertFalse(joined, "Joining should fail after deadline.");
        assertEquals(0, expiredWaitlist.getWaitlistCount(), "Waitlist should remain empty.");
    }

    /**
     *  Test: User cannot join twice (duplicate prevention).
     */
    @Test
    void testDuplicateUserCannotJoinTwice() {
        assertTrue(waitlist.joinWaitlist("user1"), "First join should succeed.");
        assertFalse(waitlist.joinWaitlist("user1"), "Second join should be rejected.");
        assertEquals(1, waitlist.getWaitlistCount(), "Count should remain 1 after duplicate attempt.");
    }

    /**
     *  Test: User can leave waitlist successfully.
     */
    @Test
    void testLeaveWaitlistSuccessfully() {
        waitlist.joinWaitlist("user1");
        boolean left = waitlist.leaveWaitlist("user1");

        assertTrue(left, "User should be able to leave successfully.");
        assertFalse(waitlist.isUserOnWaitlist("user1"), "User should no longer be on waitlist.");
        assertEquals(0, waitlist.getWaitlistCount(), "Count should decrease after leaving.");
    }

    /**
     * Test: Leaving fails if user is not on the waitlist.
     */
    @Test
    void testLeaveWaitlistUserNotFound() {
        boolean left = waitlist.leaveWaitlist("user999");

        assertFalse(left, "User not on waitlist should not be able to leave.");
    }

    /**
     * Test: Check if user is on waitlist works correctly.
     */
    @Test
    void testIsUserOnWaitlist() {
        waitlist.joinWaitlist("user1");

        assertTrue(waitlist.isUserOnWaitlist("user1"), "Existing user should return true.");
        assertFalse(waitlist.isUserOnWaitlist("user2"), "Non-existent user should return false.");
    }

    /**
     * Test: Getters and setters for Firebase serialization work as expected.
     */
    @Test
    void testGettersAndSetters() {
        // Test eventId setter/getter
        waitlist.setEventId("newEvent");
        assertEquals("newEvent", waitlist.getEventId(), "Event ID should update correctly.");

        // Test userIds setter/getter
        List<String> newList = new ArrayList<>(Arrays.asList("userA", "userB"));
        waitlist.setUserIds(newList);
        assertEquals(2, waitlist.getUserIds().size(), "User list should update correctly.");

        // Test opening and deadline setter/getter
        Date newOpen = new Date();
        Date newDeadline = new Date(newOpen.getTime() + 1000 * 60 * 60);
        waitlist.setOpening(newOpen);
        waitlist.setDeadline(newDeadline);

        assertEquals(newOpen, waitlist.getOpening(), "Opening date should match.");
        assertEquals(newDeadline, waitlist.getDeadline(), "Deadline date should match.");
    }

    /**
     * Test: Waitlist should start empty.
     */
    @Test
    void testEmptyWaitlistInitially() {
        assertEquals(0, waitlist.getWaitlistCount(), "Waitlist should start with zero users.");
    }
}
