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
                    // Update notification log
                    updateNotificationLog(notification.getId(), NotificationStatus.ACCEPTED);
                    
                    // Update waitlist entrant status to "Accepted"
                    updateWaitlistEntrantStatus(notification.getEventId(), notification.getUserId(), "Accepted");
                    
                    FirebaseEventService eventService = new FirebaseEventService();
                    eventService.addAttendee(
                            notification.getEventId(),
                            notification.getUserId(),
                            () -> {
                                Log.d("FirebaseNotificationService", "Attendee added for event "+ notification.getEventId());
                                onSuccess.run();
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
                .addOnSuccessListener(Void -> {
                    // Update notification log
                    updateNotificationLog(notification.getId(), NotificationStatus.DECLINED);
                    // Update waitlist entrant status to "Denied"
                    updateWaitlistEntrantStatus(notification.getEventId(), notification.getUserId(), "Denied");
                    onSuccess.run();
                })
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
                .update("status", NotificationStatus.SEEN.name())
                .addOnSuccessListener(Void -> {
                    // Update notification log
                    updateNotificationLog(notification.getId(), NotificationStatus.SEEN);
                });
    }

    /**
     * Updates the notification log when status changes
     * @param notificationId the ID of the notification
     * @param newStatus the new status
     */
    private void updateNotificationLog(String notificationId, NotificationStatus newStatus) {
        db.collection("notification_logs")
                .whereEqualTo("notificationId", notificationId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Update existing log
                        querySnapshot.getDocuments().get(0).getReference()
                                .update("status", newStatus.name(), "timestamp", new java.util.Date())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirebaseNotificationService", "Notification log updated: " + notificationId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseNotificationService", "Failed to update notification log", e);
                                });
                    } else {
                        // Create new log entry if not found (shouldn't happen, but safety check)
                        Log.w("FirebaseNotificationService", "Notification log not found for: " + notificationId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseNotificationService", "Failed to find notification log", e);
                });
    }

    /**
     * Sends a new notification to Firestore and logs it.
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
                    // Log the notification
                    logNotification(notification);
                    onSuccess.run();
                })
                .addOnFailureListener(onError::accept);
    }

    /**
     * Logs a notification with its status and timestamp to the notification_logs collection
     * @param notification the notification to log
     */
    private void logNotification(Notification notification) {
        NotificationLog log = new NotificationLog(
                notification.getId(),
                notification.getUserId(),
                notification.getEventId(),
                notification.getEventName(),
                notification.getStatus(),
                notification.getCreated_at(),
                notification.getType()
        );
        
        db.collection("notification_logs")
                .add(log)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirebaseNotificationService", "Notification logged: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseNotificationService", "Failed to log notification", e);
                });
    }

    /**
     * Updates the waitlist entrant status for a user in an event
     * @param eventId the event UUID
     * @param userId the user ID
     * @param newStatus the new status ("Accepted" or "Denied")
     */
    private void updateWaitlistEntrantStatus(String eventId, String userId, String newStatus) {
        // Find the event by UUID
        db.collection("events")
                .whereEqualTo("uuid", eventId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String eventDocId = querySnapshot.getDocuments().get(0).getId();
                        // Get the event to access waitlist
                        querySnapshot.getDocuments().get(0).getReference()
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    Event event = documentSnapshot.toObject(Event.class);
                                    if (event != null && event.getWaitlist() != null) {
                                        List<WaitlistEntrant> entrants = event.getWaitlist().getWaitlistEntrants();
                                        if (entrants != null) {
                                            // Find and update the entrant
                                            for (WaitlistEntrant entrant : entrants) {
                                                if (entrant != null && entrant.getUserId().equals(userId)) {
                                                    entrant.setStatus(newStatus);
                                                    break;
                                                }
                                            }
                                            // Update the event in Firestore
                                            db.collection("events").document(eventDocId)
                                                    .update("waitlist.waitlistEntrants", entrants)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("FirebaseNotificationService", 
                                                                "Waitlist entrant status updated to " + newStatus + " for user " + userId);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("FirebaseNotificationService", "Failed to update waitlist entrant status", e);
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseNotificationService", "Failed to get event document", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseNotificationService", "Failed to find event", e);
                });
    }
}
