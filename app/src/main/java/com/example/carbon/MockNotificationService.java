package com.example.carbon;

/**
 * In-memory implementation of NotificationService for UI testing without hitting Firebase.
 * Outstanding issues: state resets between runs and does not persist across process restarts.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class MockNotificationService implements NotificationService{
    private final List<Notification> mockNotifications = new ArrayList<>();

    public MockNotificationService() {
        mockNotifications.add(new Notification(
                "1", "mockUser", "EVT123", "Marathon 2025",
                "You’ve been selected for the event!", NotificationStatus.UNREAD, new Date(), "chosen"
        ));
        mockNotifications.add(new Notification(
                "2", "mockUser", "EVT456", "City Run",
                "Sorry, you were not selected.", NotificationStatus.UNREAD, new Date()
        ));

        mockNotifications.add(new Notification( "3", "mockUser", "EVT789", "Sustainability Fair",
                "You’ve been invited to join the Sustainability Fair!", NotificationStatus.UNREAD, new Date(), "invitation"
        ));
        mockNotifications.add(new Notification(
                "4", "mockUser", "EVT321", "Carbon Neutral Workshop",
                "Join the Carbon Neutral Workshop by GreenTech!", NotificationStatus.UNREAD, new Date(), "invitation"
        ));
    }

    @Override
    public void fetchNotifications(String userId, Consumer<List<Notification>> callback) {
        callback.accept(new ArrayList<>(mockNotifications));
    }

    @Override
    public void markAsAccepted(Notification notification, Runnable onSuccess, Consumer<Exception> onError) {
        notification.setStatus(NotificationStatus.ACCEPTED);
        onSuccess.run();
    }

    @Override
    public void markAsDeclined(Notification notification, Runnable onSuccess, Consumer<Exception> onError) {
        notification.setStatus(NotificationStatus.DECLINED);
        onSuccess.run();
    }

    @Override
    public void markAsSeen(Notification notification) {
        notification.setStatus(NotificationStatus.SEEN);
    }

    @Override
    public void sendNotification(Notification notification, Runnable onSuccess, Consumer<Exception> onError) {
        mockNotifications.add(notification);
        onSuccess.run();
    }
}
