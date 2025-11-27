package com.example.carbon;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageButton;
import android.content.SharedPreferences;

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
            homeButton.setOnClickListener(v -> navigateHome(activity));
        }
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                Intent intent = new Intent(activity, ProfileActivity.class);
                activity.startActivity(intent);
            });
        }

    }

    /**
     * Navigates to the appropriate home screen based on cached user role.
     * Defaults to entrant/upcoming-events view if no role cached.
     */
    public static void navigateHome(Activity activity) {
        Intent intent = new Intent(activity, getHomeActivity(activity));
        activity.startActivity(intent);
    }

    private static Class<?> getHomeActivity(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("user_prefs", Activity.MODE_PRIVATE);
        String role = prefs.getString("role", "");
        if ("organizer".equalsIgnoreCase(role)) {
            return BrowseOrganizerEventsActivity.class;
        }
        return BrowseEventsActivity.class;
    }
}
