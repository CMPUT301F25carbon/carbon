package com.example.carbon;

import java.util.Date;

/**
 * Represents a log entry for a notification, tracking its status and timestamp
 */
public class NotificationLog {
    private String notificationId;
    private String userId;
    private String eventId;
    private String eventName;
    private NotificationStatus status;
    private Date timestamp;
    private String type;

    // Required empty constructor for Firestore
    public NotificationLog() {
    }

    public NotificationLog(String notificationId, String userId, String eventId, String eventName, 
                          NotificationStatus status, Date timestamp, String type) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.status = status;
        this.timestamp = timestamp != null ? timestamp : new Date();
        this.type = type;
    }

    // Getters and setters
    /** @return source notification id */
    public String getNotificationId() {
        return notificationId;
    }

    /** @param notificationId identifier of the notification entry */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /** @return log owner id */
    public String getUserId() {
        return userId;
    }

    /** @param userId recipient identifier */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return related event id */
    public String getEventId() {
        return eventId;
    }

    /** @param eventId related event uuid/id */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /** @return related event name */
    public String getEventName() {
        return eventName;
    }

    /** @param eventName readable event title */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /** @return status at the moment of logging */
    public NotificationStatus getStatus() {
        return status;
    }

    /** @param status stored status */
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    /** @return timestamp for log entry */
    public Date getTimestamp() {
        return timestamp;
    }

    /** @param timestamp log timestamp */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /** @return notification type label */
    public String getType() {
        return type;
    }

    /** @param type notification type label */
    public void setType(String type) {
        this.type = type;
    }
}
