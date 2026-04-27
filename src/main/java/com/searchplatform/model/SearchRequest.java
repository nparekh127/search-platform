package com.searchplatform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SearchRequest {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "User is required")
    private String user;

    @NotBlank(message = "Query is required")
    @Size(min = 3, max = 15, message = "Query must be between 3 and 15 characters")
    private String query;

    @NotNull(message = "Context is required")
    private SearchContext context;

    public SearchRequest() {
    }

    public SearchRequest(String token, String user, String query, SearchContext context) {
        this.token = token;
        this.user = user;
        this.query = query;
        this.context = context;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public SearchContext getContext() {
        return context;
    }

    public void setContext(SearchContext context) {
        this.context = context;
    }
}
