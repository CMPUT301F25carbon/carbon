package com.example.carbon;

import java.util.Date;

public class Notification {

    private String id;
    private String userId;
    private String eventId;
    private String eventName;
    private String message;
    private NotificationStatus status;
    private Date created_at;
    private String type; // type of notification regular, invitation, chosen(for those chosen from the lottery pick)


    public Notification() {

    }

    public Notification(String id, String userId, String eventId, String eventName,
                        String message, NotificationStatus status, Date created_at) {
        this(id, userId, eventId, eventName, message, status, created_at, "default");
    }

    public Notification(String id, String userId, String eventId, String eventName,
                        String message, NotificationStatus status, Date created_at, String type) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.message = message;
        this.status = status;
        this.created_at = created_at != null ? created_at : new Date();
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public void setType(String type) {
        this.type = type;
    }
}


