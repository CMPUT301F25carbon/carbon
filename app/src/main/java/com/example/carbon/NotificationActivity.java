package com.example.carbon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

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

        if (USE_MOCK_SERVICE) {
            notificationService = new MockNotificationService();
        } else {
            notificationService = new FirebaseNotificationService();
        }

        loadNotifications("mockUser"); // Todo HardCoded user id for testing later to be replaced by logged in users id
        // loadNotifications(currentUser.getUid());


    }

    private void loadNotifications(String userId) {
        notificationService.fetchNotifications(userId, notifications -> {
            runOnUiThread(() -> displayNotifications(notifications));
        });
    }

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
                if ("invitation".equalsIgnoreCase(notification.getType())) {
                    InvitationDialog dialog = new InvitationDialog(
                            this, notification, notificationService, itemView
                    );
                    dialog.show();
                } else {
                    notificationService.markAsSeen(notification);
                    messageView.setTextColor(getColor(android.R.color.darker_gray));
                }
            });

            notificationContainer.addView(itemView);
        }
    }
}
