package com.example.carbon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MockNotificationServiceTest {

    @Test
    public void fetchNotificationsReturnsSeedData() {
        MockNotificationService service = new MockNotificationService();
        AtomicInteger count = new AtomicInteger();

        service.fetchNotifications("mockUser", notifications -> count.set(notifications.size()));

        assertTrue("Seed notifications should exist", count.get() >= 1);
    }

    @Test
    public void markAsAcceptedAndDeclinedUpdateStatus() {
        MockNotificationService service = new MockNotificationService();
        Notification notification = new Notification("temp", "mockUser", "EVT-1", "Sample",
                "msg", NotificationStatus.UNREAD, new Date());
        AtomicBoolean callbackRun = new AtomicBoolean(false);

        service.markAsAccepted(notification, () -> callbackRun.set(true), e -> {});
        assertEquals(NotificationStatus.ACCEPTED, notification.getStatus());
        assertTrue("Success callback should run", callbackRun.get());

        callbackRun.set(false);
        service.markAsDeclined(notification, () -> callbackRun.set(true), e -> {});
        assertEquals(NotificationStatus.DECLINED, notification.getStatus());
        assertTrue(callbackRun.get());
    }

    @Test
    public void sendNotificationAppendsToList() {
        MockNotificationService service = new MockNotificationService();
        AtomicInteger sizeBefore = new AtomicInteger();
        AtomicInteger sizeAfter = new AtomicInteger();

        service.fetchNotifications("mockUser", notifications -> sizeBefore.set(notifications.size()));

        Notification notification = new Notification("temp-2", "mockUser", "EVT-2", "City Run",
                "See you there", NotificationStatus.UNREAD, new Date(), "invitation");
        service.sendNotification(notification, () -> {}, e -> {});

        service.fetchNotifications("mockUser", notifications -> sizeAfter.set(notifications.size()));

        assertEquals(sizeBefore.get() + 1, sizeAfter.get());
    }
}
