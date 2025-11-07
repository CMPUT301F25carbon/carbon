package com.example.carbon;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNo;
    private String role;
    private boolean banned;

    public User() {}

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNo() { return phoneNo; }
    public String getRole() { return role; }

    public boolean isBanned() {
        return banned;
    }
}