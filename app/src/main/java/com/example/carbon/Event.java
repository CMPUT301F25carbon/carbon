package com.example.carbon;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Event {
    private String title;
    private String description;
    private boolean active;
    private Integer totalSpots;
    private Date eventDate;
    private String eventLocation;
    private String eventCity;
    private String eventProvince;
    private String eventCountry;
    private String category; // Event category for filtering
    private String ownerId;
    private Waitlist waitlist;
    private String uuid;
    private List<String> attendeeList = new ArrayList<>();  // to hold the attendees that are going to be attending the event
    private String imageURL;


    // Required empty public constructor for Firestore
    public Event(){}

    // Constructor for a new event with a registration deadline and opening (forces both)
    public Event(String title, String des, Integer totalSpots, Date newEventDate, String eventLocation, String eventCity, String eventProvince, String eventCountry, String ownerId, Waitlist waitlist, String imageURL) {
        this.title = title;
        this.description = des;
        this.active = true;
        this.totalSpots = totalSpots;
        this.eventDate = newEventDate;
        this.eventLocation = eventLocation;
        this.eventCity = eventCity;
        this.eventProvince = eventProvince;
        this.eventCountry = eventCountry;
        this.ownerId = ownerId;
        this.waitlist = waitlist;
        this.uuid = UUID.randomUUID().toString();
        this.attendeeList = new ArrayList<>();
        this.imageURL = imageURL;
    }


    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public Integer getTotalSpots() { return totalSpots; }
    public Date getEventDate() { return eventDate; }
    public String getEventLocation() { return eventLocation; }
    public String getEventCity() { return eventCity; }
    public String getEventProvince() { return eventProvince; }
    public String getEventCountry() { return eventCountry; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getOwnerId() { return ownerId; }
    public Waitlist getWaitlist() { return waitlist; }
    public String getUuid() { return uuid; }
    public String getImageURL() {return imageURL;}

    public List<String> getAttendeeList() {
        return attendeeList;
    }

    public void setAttendeeList(List<String> attendeeList) {
        this.attendeeList = attendeeList;
    }


    // This setter ensures Firestore Timestamp is converted to Date
    public void setEventDate(Object eventDate) {
        if (eventDate instanceof Timestamp) {
            this.eventDate = ((Timestamp) eventDate).toDate();
        } else if (eventDate instanceof Date) {
            this.eventDate = (Date) eventDate;
        }
    }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setActive(boolean active) { this.active = active; }
    public boolean getIsActive() {
        if (this.waitlist == null) return false;
        Date currentDate = new Date();
        setActive(!currentDate.after(waitlist.getDeadline()) && !currentDate.before(waitlist.getOpening()));
        return active;
    }

    /**
     * Adds a user ID to the attendee list if not already present
     */
    public void addAttendee(String userId) {
        if (attendeeList == null) {
            attendeeList = new ArrayList<>();
        }
        if (!attendeeList.contains(userId)) {
            attendeeList.add(userId);
        }
    }

    /**
     * Removes a user ID from the attendee list
     */
    public void removeAttendee(String userId) {
        if (attendeeList != null)  {
            attendeeList.remove(userId);
        }
    }

    /**
     * Checks if a given user is already in the attendee list
     */
    public boolean isAttendee(String userId) {
        return attendeeList != null && attendeeList.contains(userId);
    }

    /**
     * Returns the status of a specific user for this event
     * Possible values: "Selected", "Not Selected", "Cancelled", etc.
     */
    public String getUserStatus(String userId) {
        if (waitlist == null || waitlist.getWaitlistEntrants() == null || userId == null) {
            return "Not Selected";
        }

        for (WaitlistEntrant entrant : waitlist.getWaitlistEntrants()) {
            if (entrant != null && userId.equals(entrant.getUserId())) {
                String status = entrant.getStatus();
                return status != null ? status : "Not Selected";
            }
        }

        // User not found in waitlist
        return "Not Selected";
    }


}
