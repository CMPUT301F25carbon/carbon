package com.example.carbon;

import java.util.Date;

/**
 * Data model representing a single notification destined for a user.
 * Outstanding issues: persistence of read/seen timestamps is handled elsewhere.
 */
public class Notification {

    private String id;
    private String userId;
    private String eventId;
    private String eventName;
    private String message;
    private NotificationStatus status;
    private Date created_at;
    private String type; // type of notification regular, invitation, chosen(for those chosen from the lottery pick)


    /** Required for Firestore deserialization. */
    public Notification() {

    }

    /**
     * Creates a notification instance with a default type.
     * @param id database id (optional)
     * @param userId recipient identifier
     * @param eventId related event id/uuid
     * @param eventName related event name
     * @param message body shown to the user
     * @param status current status
     * @param created_at timestamp for creation
     */
    public Notification(String id, String userId, String eventId, String eventName,
                        String message, NotificationStatus status, Date created_at) {
        this(id, userId, eventId, eventName, message, status, created_at, "default");
    }

    /**
     * Creates a notification instance with explicit type.
     * @param id database id (optional)
     * @param userId recipient identifier
     * @param eventId related event id/uuid
     * @param eventName related event name
     * @param message body shown to the user
     * @param status current status
     * @param created_at timestamp for creation
     * @param type classification for UI handling
     */
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

    /** @return notification document id */
    public String getId() {
        return id;
    }

    /** @return recipient user id */
    public String getUserId() {
        return userId;
    }

    /** @return associated event id/uuid */
    public String getEventId() {
        return eventId;
    }

    /** @return associated event name */
    public String getEventName() {
        return eventName;
    }

    /** @return message body */
    public String getMessage() {
        return message;
    }

    /** @return current status */
    public NotificationStatus getStatus() {
        return status;
    }

    /** @return timestamp for creation */
    public Date getCreated_at() { return created_at; }
    /**
     * Sets the creation timestamp when backfilling data.
     * @param created_at creation date
     */
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    /** @return classification for UI/logic */
    public String getType() {
        return type;
    }

    /** @param id firestore-generated id */
    public void setId(String id) {
        this.id = id;
    }

    /** @param status update notification status */
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    /** @param type update classification */
    public void setType(String type) {
        this.type = type;
    }
}

