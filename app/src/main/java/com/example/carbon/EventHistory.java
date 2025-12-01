package com.example.carbon;

/**
 * Represents a user's history entry for a specific event.
 * This class encapsulates an {@link Event} object and the
 * corresponding status of the user for that event
 * The status is read-only and cannot be modified after construction.
 *
 *
 * @author Oyonti Nasir
 */

public class EventHistory {

    /** The event associated with this history entry */
    private final Event event;

    /** The status of the user for this event (read-only) */
    private final String status;  // read-only, no setter

    /**
     * Constructs an EventHistory object with the given event and status.
     *
     * @param event the {@link Event} object representing the event
     * @param status the status of the user for this event (e.g., "Cancelled")
     */
    public EventHistory(Event event, String status) {
        this.event = event;
        this.status = status;
    }

    /**
     * Returns the event associated with this history entry.
     *
     * @return the {@link Event} object
     */
    public Event getEvent() { return event; }

    /**
     * Returns the user's status for this event.
     *
     * @return a {@link String} representing the status (read-only)
     */
    public String getStatus() { return status; }
}
