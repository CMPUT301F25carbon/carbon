package com.example.carbon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;
import java.util.List;

public class EventModelTest {

    @Test
    public void addAndRemoveAttendeesRespectUniqueness() {
        Date eventDate = new Date(System.currentTimeMillis() + 86_400_000);
        Waitlist waitlist = new Waitlist(new Date(System.currentTimeMillis() - 60_000),
                new Date(System.currentTimeMillis() + 86_400_000));
        Event event = new Event("Title", "Desc", 5, eventDate,
                "123 Street", "City", "Province", "Country", "owner-1", waitlist, null);

        event.addAttendee("user-1");
        event.addAttendee("user-1");

        List<String> attendees = event.getAttendeeList();
        assertEquals("Duplicate attendees should be ignored", 1, attendees.size());

        event.removeAttendee("user-1");
        assertTrue(attendees.isEmpty());
    }

    @Test
    public void getUserStatusReflectsWaitlistEntrants() {
        Date now = new Date();
        Waitlist waitlist = new Waitlist(new Date(now.getTime() - 60_000), new Date(now.getTime() + 86_400_000));
        waitlist.joinWaitlist("user-accepted");
        waitlist.joinWaitlist("user-pending");

        waitlist.getWaitlistEntrants().get(0).setStatus("Accepted");
        waitlist.getWaitlistEntrants().get(1).setStatus("Pending");

        Event event = new Event("Title", "Desc", 5, new Date(now.getTime() + 172_800_000),
                "123 Street", "City", "Province", "Country", "owner-1", waitlist, null);

        assertEquals("Accepted", event.getUserStatus("user-accepted"));
        assertEquals("Pending", event.getUserStatus("user-pending"));
        assertEquals("Not Selected", event.getUserStatus("user-missing"));
    }

    @Test
    public void eventActiveRespectsWaitlistWindow() {
        Date now = new Date();
        Waitlist openWaitlist = new Waitlist(new Date(now.getTime() - 60_000), new Date(now.getTime() + 86_400_000));
        Event openEvent = new Event("Open", "Desc", 3, new Date(now.getTime() + 172_800_000),
                "123 Street", "City", "Province", "Country", "owner-1", openWaitlist, null);
        assertTrue(openEvent.getIsActive());

        Waitlist futureWaitlist = new Waitlist(new Date(now.getTime() + 86_400_000), new Date(now.getTime() + 172_800_000));
        Event closedEvent = new Event("Closed", "Desc", 3, new Date(now.getTime() + 172_800_000),
                "123 Street", "City", "Province", "Country", "owner-1", futureWaitlist, null);
        assertFalse(closedEvent.getIsActive());
    }
}
