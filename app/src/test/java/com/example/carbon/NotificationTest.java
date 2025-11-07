package com.example.carbon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.util.Date;

public class NotificationTest {

    @Test
    public void statusUpdateTest() {
        Notification n = new Notification("N1", "Test", "E3", "EventTest", "msg", NotificationStatus.UNREAD, new Date());
        n.setStatus(NotificationStatus.ACCEPTED);
        assertEquals(NotificationStatus.ACCEPTED, n.getStatus());
    }

    @Test
    public void typeUpdateTets() {
        Notification n = new Notification("N2", "Test1", "E5", "Event", "msg", NotificationStatus.UNREAD, new Date());
        n.setType("chosen");
        assertEquals("chosen", n.getType());

    }

    @Test
    public void createdAtNullTest() {
        Notification n = new Notification("N3", "Test2", "E7", "Event4", "msg", NotificationStatus.UNREAD, null);
        assertNotNull(n.getCreated_at());
    }
}
