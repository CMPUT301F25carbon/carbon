package com.example.carbon;

/**
 * Data model representing an uploaded event poster asset.
 * Outstanding issues: timestamp is stored as Firebase type and should be normalized on read.
 */
public class Poster {
    private String imageUrl;
    private String eventId;
    private String uploadedBy;
    private com.google.firebase.Timestamp timestamp;

    /** Empty constructor for Firestore deserialization. */
    public Poster() {}

    /** @return stored image URL */
    public String getImageUrl() { return imageUrl; }
    /** @param imageUrl URL of uploaded asset */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    /** @return owning event id */
    public String getEventId() { return eventId; }
    /** @param eventId identifier of related event */
    public void setEventId(String eventId) { this.eventId = eventId; }
    /** @return uploader id */
    public String getUploadedBy() { return uploadedBy; }
    /** @param uploadedBy id of uploader */
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}
