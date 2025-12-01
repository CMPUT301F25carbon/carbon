package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Activity that displays all notifications for a user.
 * Each notification shows information about events and allows
 * the user to interact depending on the type of notification
 */
public class NotificationActivity extends AppCompatActivity {
    private LinearLayout notificationContainer;
    private TextView emptyView;
    private ProgressBar progressBar;
    private NotificationService notificationService;
    private static final boolean USE_MOCK_SERVICE = false; //set to false for firebase
    private com.google.firebase.firestore.ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifcation);

        UIHelper.setupHeaderAndMenu(this);

        notificationContainer = findViewById(R.id.notifications_list_container);
        emptyView = findViewById(R.id.notifications_empty);
        progressBar = findViewById(R.id.notifications_progress);

        // Choose between mock and firebase service
        if (USE_MOCK_SERVICE) {
            notificationService = new MockNotificationService();
        } else {
            notificationService = new FirebaseNotificationService();
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view notifications", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadNotifications(uid);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }

    /**
     * Fetches Notifications for a specific user and displays them.
     * @param userId the ID of the user whose notifications are being loaded
     */
    private void loadNotifications(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        listener = db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("created_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((snap, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null || snap == null) {
                        emptyView.setVisibility(View.VISIBLE);
                        return;
                    }
                    java.util.List<Notification> list = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snap) {
                        Notification n = doc.toObject(Notification.class);
                        n.setId(doc.getId());
                        list.add(n);
                    }
                    displayNotifications(list);
                });
    }

    /**
     * Displays a list of notifications in the layout.
     * Each notification item reacts differently based on its type:
     * - "invitation": Opens an invitation dialog
     * - "chosen": Opens event details
     * - Other: Marks notification as seen
     * @param notifications the list of notifications to display
     */
    private void displayNotifications(List<Notification> notifications) {
        LayoutInflater inflater = LayoutInflater.from(this);
        notificationContainer.removeAllViews();

        for (Notification notification : notifications) {
            View itemView = inflater.inflate(R.layout.item_notification, notificationContainer, false);

            TextView titleView = itemView.findViewById(R.id.notification_title);
            TextView messageView = itemView.findViewById(R.id.notification_message);
            LinearLayout actions = itemView.findViewById(R.id.notification_actions);
            Button acceptBtn = itemView.findViewById(R.id.btn_accept_notification);
            Button declineBtn = itemView.findViewById(R.id.btn_decline_notification);

            titleView.setText(notification.getEventName());
            messageView.setText(notification.getMessage());

            boolean actionable = "invitation".equalsIgnoreCase(notification.getType()) || "chosen".equalsIgnoreCase(notification.getType());
            actions.setVisibility(actionable ? View.VISIBLE : View.GONE);

            acceptBtn.setOnClickListener(v -> {
                if (notification.getEventId() == null || notification.getEventId().isEmpty()) {
                    Toast.makeText(this, "Missing event info for this notification", Toast.LENGTH_SHORT).show();
                    return;
                }
                notificationService.markAsAccepted(notification,
                        () -> runOnUiThread(() -> {
                            Toast.makeText(this, "Event accepted", Toast.LENGTH_SHORT).show();
                            notificationContainer.removeView(itemView);
                            emptyView.setVisibility(notificationContainer.getChildCount() == 0 ? View.VISIBLE : View.GONE);
                            Intent intent = new Intent(this, EventDetailsActivity.class);
                            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, notification.getEventId());
                            startActivity(intent);
                        }),
                        e -> runOnUiThread(() -> Toast.makeText(this, "Unable to accept right now. Please try again.", Toast.LENGTH_LONG).show()));
            });

            declineBtn.setOnClickListener(v -> {
                if (notification.getEventId() == null || notification.getEventId().isEmpty()) {
                    Toast.makeText(this, "Missing event info for this notification", Toast.LENGTH_SHORT).show();
                    return;
                }
                notificationService.markAsDeclined(notification,
                        () -> runOnUiThread(() -> {
                            Toast.makeText(this, "Event declined", Toast.LENGTH_SHORT).show();
                            notificationContainer.removeView(itemView);
                            emptyView.setVisibility(notificationContainer.getChildCount() == 0 ? View.VISIBLE : View.GONE);
                        }),
                        e -> runOnUiThread(() -> Toast.makeText(this, "Unable to decline right now. Please try again.", Toast.LENGTH_LONG).show()));
            });

            itemView.setOnClickListener(v -> {
                if (!actionable) {
                    notificationService.markAsSeen(notification);
                    messageView.setTextColor(getColor(android.R.color.darker_gray));
                }
            });

            notificationContainer.addView(itemView);
        }

        emptyView.setVisibility(notifications == null || notifications.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
