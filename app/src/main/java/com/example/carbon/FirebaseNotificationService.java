package com.example.carbon;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles notification-related operations using Firebase Firestore
 * This class provides methods to send, update, and fetch notifications
 * for a given user, as well as manage event attendance when notifications
 * are accepted or declined
 */
public class FirebaseNotificationService implements NotificationService{
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Fetches all notifications for a specific user from firestore
     * @param userId the ID of the user whose notifications should be retrieved
     * @param callback the function that receives the list of notifications
     */
    @Override
    public void fetchNotifications(String userId, Consumer<List<Notification>> callback) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Notification> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Notification n = doc.toObject(Notification.class);
                        n.setId(doc.getId());
                        list.add(n);
                    }
                    callback.accept(list);
                })
                .addOnFailureListener(e -> callback.accept(new ArrayList<>()));
    }

    /**
     * Marks a notification as accepted and adds the user as an attendee for the related event.
     * @param notification the notification being accepted
     * @param onSuccess callback to run when the operation completes successfully
     * @param onError callback to handle any error that occurs
     */
    @Override
    public void markAsAccepted(Notification notification, Runnable onSuccess, Consumer<Exception> onError) {
        db.collection("notifications").document(notification.getId())
                .update("status", NotificationStatus.ACCEPTED.name())
                .addOnSuccessListener(Void -> {
                    FirebaseEventService eventService = new FirebaseEventService();
                    eventService.addAttendee(
                            notification.getEventId(),
                            notification.getUserId(),
                            () -> {
                                Log.d("FirebaseNotificationService", "Attendee added for event "+ notification.getEventId());
                            },
                            e -> {
                                Log.e("FirebaseNotificationService", "Failed to add attendee", e);
                                onError.accept(e);
                            }
                    );
                })
                .addOnFailureListener(onError::accept);

    }

    /**
     * Marks a notification as declined
     * As mentioned in the TODO, once the re-selection process is complete will be implemented below
     * @param notification the declineed notification
     * @param onSuccess callback to run when the update succeeds
     * @param onError callback to handle any errors
     */
    @Override
    public void markAsDeclined(Notification notification, Runnable onSuccess, Consumer<Exception> onError) {
        db.collection("notifications").document(notification.getId())
                .update("status", NotificationStatus.DECLINED.name())
                .addOnSuccessListener(Void -> onSuccess.run())
                .addOnFailureListener(onError::accept);
        // TODO: Trigger next user selection in event waitlist logic
    }

    /**
     * Marks a notification as seen in Firestore
     * @param notification the notification that is being marked as seen
     */
    @Override
    public void markAsSeen(Notification notification) {
        db.collection("notifications").document(notification.getId())
                .update("status", NotificationStatus.SEEN.name());
    }

    /**
     * Sends a new notification to Firestore.
     *
     * @param notification the notification to send
     * @param onSuccess callback to run when successfully saved
     * @param onError callback to handle any errors
     */
    @Override
    public void sendNotification(Notification notification, Runnable onSuccess, Consumer<Exception> onError) {
        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    notification.setId(documentReference.getId());
                    onSuccess.run();
                })
                .addOnFailureListener(onError::accept);
    }
}
