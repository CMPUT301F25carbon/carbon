package com.example.carbon;

import java.util.Date;
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
    private String ownerId;
    private Waitlist waitlist;
    private String uuid;


    // Required empty public constructor for Firestore
    public Event(){}

    // Constructor for a new event with a registration deadline and opening (forces both)
    public Event(String title, String des, Integer totalSpots, Date newEventDate, String eventLocation, String eventCity, String eventProvince, String eventCountry, String ownerId, Waitlist waitlist) {
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
    public String getOwnerId() { return ownerId; }
    public Waitlist getWaitlist() { return waitlist; }
    public String getUuid() { return uuid; }



    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setActive(boolean active) { this.active = active; }
    public boolean getIsActive() {
        if (this.waitlist == null) return false;
        Date currentDate = new Date();
        setActive(!currentDate.after(waitlist.getDeadline()) && !currentDate.before(waitlist.getOpening()));
        return active;
    }

}
