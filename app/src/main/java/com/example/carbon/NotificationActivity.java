package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

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
    private NotificationService notificationService;
    private static final boolean USE_MOCK_SERVICE = false; //set to false for firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifcation);

        UIHelper.setupHeaderAndMenu(this);

        notificationContainer = findViewById(R.id.notifications_list_container);

        // Choose between mock and firebase service
        if (USE_MOCK_SERVICE) {
            notificationService = new MockNotificationService();
        } else {
            notificationService = new FirebaseNotificationService();
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadNotifications(uid);


    }

    /**
     * Fetches Notifications for a specific user and displays them.
     * @param userId the ID of the user whose notifications are being loaded
     */
    private void loadNotifications(String userId) {
        notificationService.fetchNotifications(userId, notifications -> {
            runOnUiThread(() -> displayNotifications(notifications));
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
                notificationService.markAsAccepted(notification,
                        () -> runOnUiThread(() -> Toast.makeText(this, "Accepted", Toast.LENGTH_SHORT).show()),
                        e -> runOnUiThread(() -> Toast.makeText(this, "Failed to accept", Toast.LENGTH_SHORT).show()));
            });

            declineBtn.setOnClickListener(v -> {
                notificationService.markAsDeclined(notification,
                        () -> runOnUiThread(() -> Toast.makeText(this, "Declined", Toast.LENGTH_SHORT).show()),
                        e -> runOnUiThread(() -> Toast.makeText(this, "Failed to decline", Toast.LENGTH_SHORT).show()));
            });

            itemView.setOnClickListener(v -> {
                if (!actionable) {
                    notificationService.markAsSeen(notification);
                    messageView.setTextColor(getColor(android.R.color.darker_gray));
                }
            });

            notificationContainer.addView(itemView);
        }
    }
}
