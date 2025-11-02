package com.example.carbon;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FirebaseNotificationService implements NotificationService{
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    @Override
    public void markAsAccepted(Notification notification, Runnable onSuccess, Consumer<Exception> onError) {
        db.collection("notifications").document(notification.getId())
                .update("status", NotificationStatus.ACCEPTED.name())
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onError::accept);
        // TODO: Trigger attendee registration update for the associated eventId

    }

    @Override
    public void markAsDeclined(Notification notification, Runnable onSuccess, Consumer<Exception> onError) {
        db.collection("notifications").document(notification.getId())
                .update("status", NotificationStatus.DECLINED.name())
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onError::accept);
        // TODO: Trigger next user selection in event waitlist logic
    }

    @Override
    public void markAsSeen(Notification notification) {
        db.collection("notifications").document(notification.getId())
                .update("status", NotificationStatus.SEEN.name());
    }


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
