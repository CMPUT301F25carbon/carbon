package com.example.carbon;

import java.util.List;
import java.util.function.Consumer;

public interface NotificationService {
    void fetchNotifications(String userId, Consumer<List<Notification>> callback);
    void markAsAccepted(Notification notification, Runnable onSuccess, Consumer<Exception> onError);
    void markAsDeclined(Notification notification, Runnable onSuccess, Consumer<Exception> onError);
    void markAsSeen(Notification notification);
    void sendNotification(Notification notification, Runnable onSuccess, Consumer<Exception> onError);

}
