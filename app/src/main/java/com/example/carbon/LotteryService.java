package com.example.carbon;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Implements:
 *  - US 02.05.02 Sample N Attendees
 *  - US 02.05.03 Draw Replacement
 *  - US 02.05.01 / 02.07.02 Notify Selected Entrants
 */
public class LotteryService {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final NotificationService notifier = new NotificationService();
    private final Executor bg = Executors.newSingleThreadExecutor();

    public interface Callback {
        /** @param winnersAdded entries set to WON during this call
         *  @param remainingCapacity capacity left after this call
         *  @param info optional note (null if none) */
        void onComplete(int winnersAdded, int remainingCapacity, String info);
        void onError(Exception e);
    }

    /** US 02.05.02: randomly pick up to N from PENDING, constrained by remaining capacity. */
    public void sampleNAttendees(@NonNull String eventId, int n, @NonNull Callback cb) {
        drawInternal(eventId, n, false, cb);
    }

    /** US 02.05.03: fill open slots (e.g., cancellations) by sampling replacements from PENDING. */
    public void drawReplacement(@NonNull String eventId, int replacementCap, @NonNull Callback cb) {
        drawInternal(eventId, replacementCap, true, cb);
    }

    private void drawInternal(String eventId, int requestCount, boolean isReplacement, Callback cb) {
        bg.execute(() -> {
            try {
                // 1) Load event
                DocumentSnapshot evt = Tasks.await(db.collection("events").document(eventId).get());
                if (!evt.exists()) throw new IllegalStateException("Event not found: " + eventId);
                Event event = evt.toObject(Event.class);
                if (event == null) throw new IllegalStateException("Event parse error");

                // 2) Count current winners to compute remaining capacity
                var winnersSnap = Tasks.await(
                        db.collection("entries")
                                .whereEqualTo("eventId", eventId)
                                .whereEqualTo("status", EntryStatus.WON.name())
                                .get()
                );
                int currentWinners = winnersSnap.size();
                int remainingCapacity = Math.max(0, event.capacity - currentWinners);
                if (remainingCapacity == 0) {
                    cb.onComplete(0, 0, "No capacity left.");
                    return;
                }

                // 3) Fetch PENDING entries
                var pendingSnap = Tasks.await(
                        db.collection("entries")
                                .whereEqualTo("eventId", eventId)
                                .whereEqualTo("status", EntryStatus.PENDING.name())
                                .get()
                );
                List<Entry> pending = new ArrayList<>();
                for (var d : pendingSnap.getDocuments()) {
                    Entry e = d.toObject(Entry.class);
                    if (e != null) { e.id = d.getId(); pending.add(e); }
                }
                if (pending.isEmpty()) {
                    cb.onComplete(0, remainingCapacity, "No pending entrants.");
                    return;
                }

                // 4) Shuffle + select TAKE = min(request, capacityLeft, pending.size)
                Collections.shuffle(pending);
                int take = Math.min(Math.min(requestCount, remainingCapacity), pending.size());
                List<Entry> winners = new ArrayList<>(pending.subList(0, take));

                // 5) Batch update statuses to WON
                var batch = db.batch();
                for (Entry w : winners) {
                    var ref = db.collection("entries").document(w.id);
                    batch.update(ref, "status", EntryStatus.WON.name());
                }
                Tasks.await(batch.commit());

                // 6) Notify winners
                for (Entry w : winners) {
                    Tasks.await(notifier.notifyUser(
                            w.userId,
                            isReplacement ? "Replacement spot!" : "You won a spot!",
                            (event.title != null ? event.title : "event") + " — see app for details."
                    ));
                }

                cb.onComplete(take, remainingCapacity - take, null);

            } catch (Exception e) {
                cb.onError(e);
            }
        });
    }
}
