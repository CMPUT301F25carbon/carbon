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
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

