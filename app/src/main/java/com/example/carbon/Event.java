package com.example.carbon;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Data model for an event, including metadata, location, scheduling, and waitlist details.
 * Outstanding issues: attendee and waitlist updates assume Firestore writes succeed elsewhere.
 */
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


    /** Required empty public constructor for Firestore. */
    public Event(){}

    /**
     * Creates a new event instance with provided metadata and waitlist.
     * @param title display title for the event
     * @param des description copy
     * @param totalSpots total number of seats available
     * @param newEventDate scheduled date of the event
     * @param eventLocation street address or venue
     * @param eventCity city portion of the address
     * @param eventProvince province or state portion of the address
     * @param eventCountry country portion of the address
     * @param ownerId organizer identifier
     * @param waitlist waitlist instance managing entrants
     * @param imageURL optional poster URL
     */
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


    /** @return title of the event */
    public String getTitle() { return title; }
    /** @return description of the event */
    public String getDescription() { return description; }
    /** @return whether the event is currently active */
    public boolean isActive() { return active; }
    /** @return total available seats */
    public Integer getTotalSpots() { return totalSpots; }
    /** @return scheduled event date */
    public Date getEventDate() { return eventDate; }
    /** @return location line for the event */
    public String getEventLocation() { return eventLocation; }
    /** @return event city */
    public String getEventCity() { return eventCity; }
    /** @return event province or state */
    public String getEventProvince() { return eventProvince; }
    /** @return event country */
    public String getEventCountry() { return eventCountry; }
    /** @return category assigned for filtering */
    public String getCategory() { return category; }
    /** Sets the event category label for filtering. */
    public void setCategory(String category) { this.category = category; }
    /** @return organizer/owner identifier */
    public String getOwnerId() { return ownerId; }
    /** @return waitlist backing this event */
    public Waitlist getWaitlist() { return waitlist; }
    /** @return unique identifier generated at creation time */
    public String getUuid() { return uuid; }
    /** @return URL to the event poster image */
    public String getImageURL() {return imageURL;}

    /** @return mutable list of attendee identifiers */
    public List<String> getAttendeeList() {
        return attendeeList;
    }

    /**
     * Replace attendee roster with provided list.
     * @param attendeeList list of attendee user ids
     */
    public void setAttendeeList(List<String> attendeeList) {
        this.attendeeList = attendeeList;
    }


    /**
     * Setter ensures Firestore Timestamp is converted to Date.
     * @param eventDate either {@link Timestamp} or {@link Date}
     */
    public void setEventDate(Object eventDate) {
        if (eventDate instanceof Timestamp) {
            this.eventDate = ((Timestamp) eventDate).toDate();
        } else if (eventDate instanceof Date) {
            this.eventDate = (Date) eventDate;
        }
    }

    /** @param title updated display title */
    public void setTitle(String title) { this.title = title; }
    /** @param description updated description */
    public void setDescription(String description) { this.description = description; }
    /** @param active whether event remains open */
    public void setActive(boolean active) { this.active = active; }
    /**
     * Evaluates whether the event is open based on waitlist window.
     * @return true when current date is between opening and deadline; false otherwise
     */
    public boolean getIsActive() {
        if (this.waitlist == null) return false;
        Date currentDate = new Date();
        setActive(!currentDate.after(waitlist.getDeadline()) && !currentDate.before(waitlist.getOpening()));
        return active;
    }

    /**
     * Adds a user ID to the attendee list if not already present.
     * @param userId identifier of attendee to add
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
     * Removes a user ID from the attendee list.
     * @param userId identifier of attendee to remove
     */
    public void removeAttendee(String userId) {
        if (attendeeList != null)  {
            attendeeList.remove(userId);
        }
    }

    /**
     * Checks if a given user is already in the attendee list.
     * @param userId identifier to look for
     * @return true when attendee already registered
     */
    public boolean isAttendee(String userId) {
        return attendeeList != null && attendeeList.contains(userId);
    }

    /**
     * Returns the status of a specific user for this event.
     * Possible values: "Selected", "Not Selected", "Cancelled", etc.
     * @param userId identifier to check against waitlist entrants
     * @return status string; defaults to "Not Selected" when unknown
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
