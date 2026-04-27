package com.searchplatform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationServiceTest {

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService();
        Map<String, String> users = new HashMap<>();
        users.put("admin", "admin123");
        users.put("user1", "password1");
        authenticationService.setUsers(users);
    }

    @Test
    void authenticate_shouldReturnTrueForValidCredentials() {
        assertTrue(authenticationService.authenticate("admin", "admin123"));
    }

    @Test
    void authenticate_shouldReturnFalseForInvalidPassword() {
        assertFalse(authenticationService.authenticate("admin", "wrongpassword"));
    }

    @Test
    void authenticate_shouldReturnFalseForNonExistentUser() {
        assertFalse(authenticationService.authenticate("unknown", "password"));
    }

    @Test
    void authenticate_shouldReturnFalseForNullUsername() {
        assertFalse(authenticationService.authenticate(null, "password"));
    }

    @Test
    void authenticate_shouldReturnFalseForNullPassword() {
        assertFalse(authenticationService.authenticate("admin", null));
    }

    @Test
    void authenticate_shouldReturnFalseForBothNull() {
        assertFalse(authenticationService.authenticate(null, null));
    }
}
