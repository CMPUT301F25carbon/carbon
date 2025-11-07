package com.example.carbon;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Activity that displays all notifications for a user.
 * Each notification shows information about events and allows
 * the user to interact depending on the type of notification
 */
public class NotificationActivity extends AppCompatActivity {
    private LinearLayout notificationContainer;
    private NotificationService notificationService;
    private static final boolean USE_MOCK_SERVICE = true; //set to false for firebase

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

        loadNotifications("mockUser"); // Todo HardCoded user id for testing later to be replaced by logged in users id
        // loadNotifications(currentUser.getUid());


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

            titleView.setText(notification.getEventName());
            messageView.setText(notification.getMessage());

            itemView.setOnClickListener(v -> {
                if ("invitation".equalsIgnoreCase(notification.getType())) {    // This will open the accept/decline dialog for invitation notifications
                    InvitationDialog dialog = new InvitationDialog(
                            this, notification, notificationService, itemView
                    );
                    dialog.show();
                } else if ("chosen".equalsIgnoreCase(notification.getType())) {
                    notificationService.markAsSeen(notification);
                    messageView.setTextColor(getColor(android.R.color.darker_gray));
                    Intent intent = new Intent(this, EventDetailsActivity.class);
                    intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, notification.getEventId());
                    intent.putExtra(EventDetailsActivity.EXTRA_EVENT_TITLE, notification.getEventName());
                    intent.putExtra(EventDetailsActivity.EXTRA_EVENT_DATE, "Unknown"); // For now as placeholder, will change implementation of notification system
                    intent.putExtra(EventDetailsActivity.EXTRA_EVENT_COUNTS, "");
                    startActivity(intent);

                } else {
                    notificationService.markAsSeen(notification);
                    messageView.setTextColor(getColor(android.R.color.darker_gray));
                }
            });

            notificationContainer.addView(itemView);
        }
    }
}
