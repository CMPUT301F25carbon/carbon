package com.example.carbon;

import java.util.ArrayList;
import java.util.List;

public class EventList {
    private ArrayList<Event> events;

    public EventList() {
        events = new ArrayList<>();
    }

    // Add a single event to the list
    public void addEvent(Event e) {
        events.add(e);
    }

    // Return all events
    public List<Event> getAllEvents() {
        return events;
    }

    // Return only the events that are currently open/joinable : US 01.01.03
    public List<Event> getJoinableEvents() {
        List<Event> joinable = new ArrayList<>();
        for (Event e : events) {
            if (e.getIsActive()) { // uses your Event.getIsActive() method
                joinable.add(e);
            }
        }
        return joinable;
    }

}
