package com.example.carbon;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageButton;

public class UIHelper {

    public static void setupHeaderAndMenu(Activity activity) {
        ImageButton settingsButton = activity.findViewById(R.id.settings_button);
        ImageButton notificationsButton = activity.findViewById(R.id.notification_button);

        ImageButton backButton = activity.findViewById(R.id.back_button);
        ImageButton homeButton = activity.findViewById(R.id.home_button);
        ImageButton profileButton = activity.findViewById(R.id.profile_button);

        if (backButton != null) {
            backButton.setOnClickListener(v -> activity.finish());
        }

        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivity(intent);
            });
        }

        if (notificationsButton != null) {
            notificationsButton.setOnClickListener(v -> {
                Intent intent = new Intent(activity, NotificationActivity.class);
                activity.startActivity(intent);
            });
        }

        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent intent = new Intent(activity, SelectedListActivity.class);
                intent.putExtra("EVENT_ID", "5687b254-c268-4a16-9705-9b51573324ba"); // <-- jouw event ID
                activity.startActivity(intent);
            });
        }
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                Intent intent = new Intent(activity, ProfileActivity.class);
                activity.startActivity(intent);
            });
        }

    }
}
