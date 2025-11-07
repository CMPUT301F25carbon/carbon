package com.example.carbon;

import java.util.List;
import java.util.function.Consumer;

/**
 * Defines the operations for managing notifications in the system
 * This interface abstracts both mock and Firebase implementations
 * for fetching, updating, and sending notifications
 */
public interface NotificationService {
    /**
     * Retrieves all notifications for a given user
     * @param userId the ID of the user whose notifications are being fetched
     * @param callback to handle the list of retrieved notifications
     */
    void fetchNotifications(String userId, Consumer<List<Notification>> callback);

    /**
     * Retrieves all notifications for a given user.
     * @param notification the notification being accepted
     * @param onSuccess a callback invoked when the operation succeeds
     * @param onError a callback invoked when the operation fails
     */
    void markAsAccepted(Notification notification, Runnable onSuccess, Consumer<Exception> onError);

    /**
     * Marks the specified notification as declined and performs any related logic,
     * such as triggering the waitlist process
     * @param notification the notification being declined
     * @param onSuccess a callback invoked when the operation succeed
     * @param onError a callback invoked when the operation fails
     */
    void markAsDeclined(Notification notification, Runnable onSuccess, Consumer<Exception> onError);

    /**
     * Marks the specified notification as seen
     * @param notification the notification to mark as seen
     */
    void markAsSeen(Notification notification);

    /**
     * Sends a new notification to the database or mock service
     * @param notification the notification to be sent
     * @param onSuccess a callback invoked when the operation succeeds
     * @param onError a callback invoked when the operation fails
     */
    void sendNotification(Notification notification, Runnable onSuccess, Consumer<Exception> onError);

}
