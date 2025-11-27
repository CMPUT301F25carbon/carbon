package com.example.carbon;

public class Poster {
    private String imageUrl;
    private String eventId;
    private String uploadedBy;
    private com.google.firebase.Timestamp timestamp;

    public Poster() {}

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}