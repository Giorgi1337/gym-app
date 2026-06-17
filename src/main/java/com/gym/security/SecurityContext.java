package com.gym.security;

import com.gym.exception.AuthenticationException;

public class SecurityContext {

    private static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<>();

    private SecurityContext() {}

    public static void setCurrentUsername(String username) {
        CURRENT_USERNAME.set(username);
    }

    public static String getCurrentUsername() {
        String username = CURRENT_USERNAME.get();
        if (username == null) {
            throw new AuthenticationException("No authenticated user in context");
        }
        return username;
    }

    public static void clear() {
        CURRENT_USERNAME.remove();
    }
}
