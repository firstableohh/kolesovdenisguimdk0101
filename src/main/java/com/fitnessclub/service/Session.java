package com.fitnessclub.service;

import com.fitnessclub.model.Role;
import com.fitnessclub.model.User;

public final class Session {
    private static User currentUser;

    private Session() {}

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isRole(Role r) {
        return currentUser != null && currentUser.getRole() == r;
    }
}
