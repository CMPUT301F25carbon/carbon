package com.example.carbon;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.function.Consumer;

public class FirebaseEventService {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Adds a user ID to the attendee list given an event document
     *
     * @param eventId Firestore document ID of the event
     * @param userId Firebase UID of the user who accepted the event
     * @param onSuccess Callback when the operation succeeds
     * @param onError Callback when the operation fails
     */
    public void addAttendee(String eventId, String userId, Runnable onSuccess, Consumer<Exception> onError) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            event.addAttendee(userId);
                            db.collection("events").document(eventId)
                                    .update("attendees", event.getAttendeeList())
                                    .addOnSuccessListener(Void -> {
                                        Log.d("FirebaseEventService", "Attendee added: " + userId);
                                        onSuccess.run();
                                    })
                                    .addOnFailureListener(onError::accept);
                        } else {
                            onError.accept(new Exception("Event object is null"));
                        }
                    } else {
                        onError.accept(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(onError::accept);
    }

    /**
     * Fetches a list of attendee user IDs for a specific event
     *
     * @param eventId Firestore document id
     * @param onSuccess Callback returning the attendee list
     * @param onError Callback for errors
     */
    public void getAttendees(String eventId, Consumer<List<String>> onSuccess, Consumer<Exception> onError) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null && event.getAttendeeList() != null ){
                        onSuccess.accept(event.getAttendeeList());
                    } else {
                        onSuccess.accept(java.util.Collections.emptyList());
                    }
                })
                .addOnFailureListener(onError::accept);
    }
}
