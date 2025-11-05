package com.example.carbon;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class EventList {

    private static final String TAG = "EventList";

    // Callback interface to handle asynchronous results
    public interface EventListCallback {
        void onEventsFetched(ArrayList<Event> events);
        void onError(Exception e);
    }

    // No longer need to store the list as a field.
    // The data is passed directly to the callback.
    public EventList() {
        // Constructor can be empty now
    }

    /**
     * Asynchronously fetches all events from Firebase.
     * @param callback The callback to be invoked when the fetch is complete or fails.
     */
    public void fetchAllEvents(EventListCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ArrayList<Event> fetchedEvents = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            try {
                                Event event = document.toObject(Event.class);
                                if (event != null) {
                                    fetchedEvents.add(event);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to Event object", e);
                            }
                        }
                        // Data is ready, send it back via the callback
                        callback.onEventsFetched(fetchedEvents);
                    } else {
                        // An error occurred, send it back via the callback
                        Log.w(TAG, "Error getting documents.", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    /**
     * Asynchronously fetches events for a specific organizer.
     * @param organizerId The ID of the event owner.
     * @param callback The callback to be invoked when the fetch is complete or fails.
     */
    public void fetchOrganizerEvents(String organizerId, EventListCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("events").whereEqualTo("ownerId", organizerId);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ArrayList<Event> fetchedEvents = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    try {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            fetchedEvents.add(event);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error converting document to Event object", e);
                    }
                }
                // Data is ready, send it back via the callback
                callback.onEventsFetched(fetchedEvents);
            } else {
                // An error occurred, send it back via the callback
                Log.w(TAG, "Error getting documents for organizer.", task.getException());
                callback.onError(task.getException());
            }
        });
    }

    // Add a single event to the list
    public void addEvent(Event e) {
        events.add(e);
    }

    // Return all events
    public List<Event> getAllEvents() {
        return events;
    }

    // Return only the events that are currently open/joinable : US 01.01.03
    public List<Event> getJoinableEvents() {
        List<Event> joinable = new ArrayList<>();
        for (Event e : events) {
            if (e.getIsActive()) { // uses your Event.getIsActive() method
                joinable.add(e);
            }
        }
        return joinable;
    }

}
