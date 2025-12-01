package com.example.carbon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;

public class WaitlistTest {

    @Test
    public void joinWithinWindowAddsEntrant() {
        Date opening = new Date(System.currentTimeMillis() - 60_000);
        Date deadline = new Date(System.currentTimeMillis() + 86_400_000);
        Waitlist waitlist = new Waitlist(opening, deadline, 2);

        assertTrue(waitlist.joinWaitlist("user-1"));
        assertEquals(1, waitlist.getWaitlistCount());
        assertTrue(waitlist.isUserOnWaitlist("user-1"));
    }

    @Test
    public void joinRejectsDuplicatesAndCapacity() {
        Date opening = new Date(System.currentTimeMillis() - 60_000);
        Date deadline = new Date(System.currentTimeMillis() + 86_400_000);
        Waitlist waitlist = new Waitlist(opening, deadline, 1);

        assertTrue(waitlist.joinWaitlist("user-1"));
        assertFalse("Duplicate entries should be rejected", waitlist.joinWaitlist("user-1"));
        assertFalse("Capacity should cap additional entrants", waitlist.joinWaitlist("user-2"));
        assertEquals(1, waitlist.getWaitlistCount());
    }

    @Test
    public void leaveWaitlistRemovesEntrant() {
        Date opening = new Date(System.currentTimeMillis() - 60_000);
        Date deadline = new Date(System.currentTimeMillis() + 86_400_000);
        Waitlist waitlist = new Waitlist(opening, deadline, 3);

        waitlist.joinWaitlist("user-1");
        waitlist.joinWaitlist("user-2");

        assertTrue(waitlist.leaveWaitlist("user-1"));
        assertFalse(waitlist.isUserOnWaitlist("user-1"));
        assertEquals(1, waitlist.getWaitlistCount());
    }

    @Test
    public void joinOutsideWindowFails() {
        Date opening = new Date(System.currentTimeMillis() - 86_400_000);
        Date deadline = new Date(System.currentTimeMillis() - 3_600_000);
        Waitlist waitlist = new Waitlist(opening, deadline, 2);

        assertFalse("Signups should be blocked after the deadline", waitlist.joinWaitlist("late-user"));
        assertEquals(0, waitlist.getWaitlistCount());
    }
}
