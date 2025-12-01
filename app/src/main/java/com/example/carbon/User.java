package com.example.carbon;

/**
 * Lightweight user profile record used throughout the app.
 * Outstanding issues: ban reason and roles beyond entrant/organizer are not yet captured.
 */
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNo;
    private String role;
    private boolean banned;

    /** Firestore/serialization constructor. */
    public User() {}

    /** @return user's given name */
    public String getFirstName() { return firstName; }
    /** @return user's surname */
    public String getLastName() { return lastName; }
    /** @return primary email */
    public String getEmail() { return email; }
    /** @return phone number string */
    public String getPhoneNo() { return phoneNo; }
    /** @return current role (entrant, organizer, admin) */
    public String getRole() { return role; }

    /** @return true when the account is blocked from activity */
    public boolean isBanned() {
        return banned;
    }
}
