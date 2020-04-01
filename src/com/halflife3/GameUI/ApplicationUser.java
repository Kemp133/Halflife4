package com.halflife3.GameUI;

public class ApplicationUser {
    public String username;
    public boolean isValidSession = false;

    public ApplicationUser() {}

    public ApplicationUser(String username, boolean isValidSession) {
        this.username = username;
        this.isValidSession = isValidSession;
    }
}