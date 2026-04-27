package com.searchplatform.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ConfigurationProperties(prefix = "search.auth")
public class AuthenticationService {

    private Map<String, String> users = new HashMap<>();

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String storedPassword = users.get(username);
        return password.equals(storedPassword);
    }
}
