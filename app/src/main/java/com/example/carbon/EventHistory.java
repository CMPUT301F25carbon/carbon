package com.example.carbon;

public class EventHistory {

    private final Event event;
    private final String status;  // read-only, no setter

    public EventHistory(Event event, String status) {
        this.event = event;
        this.status = status;
    }

    public Event getEvent() { return event; }
    public String getStatus() { return status; }
}
