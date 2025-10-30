package com.example.carbon;

import java.util.Date;

public class Event {
    private String title;
    private String description;
    private boolean active;
    private Integer totalSpots;
    private Date eventDate;



    public Event(String title, String des, Integer totalSpots, Date newEventDate) {
        this.title = title;
        this.description = des;
        this.active = true;
        this.totalSpots = totalSpots;
        this.eventDate = newEventDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void deactivateEvent() {
        active = false;
    }

    public void enableEvent() {
        active = true;
    }

}
