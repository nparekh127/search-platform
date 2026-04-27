package com.searchplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchplatform.model.Position;
import com.searchplatform.model.SearchContext;
import com.searchplatform.model.SearchRequest;
import com.searchplatform.service.AuthenticationService;
import com.searchplatform.service.ElasticsearchService;
import com.searchplatform.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private ElasticsearchService elasticsearchService;

    @Test
    void login_shouldReturnTokenForValidCredentials() throws Exception {
        when(authenticationService.authenticate("admin", "admin123")).thenReturn(true);
        when(jwtService.generateToken("admin")).thenReturn("test-jwt-token");

        mockMvc.perform(post("/search/login")
                        .header("user", "admin")
                        .header("password", "admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"));
    }

    @Test
    void login_shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        when(authenticationService.authenticate("admin", "wrong")).thenReturn(false);

        mockMvc.perform(post("/search/login")
                        .header("user", "admin")
                        .header("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void login_shouldReturnBadRequestWhenMissingUserHeader() throws Exception {
        mockMvc.perform(post("/search/login")
                        .header("password", "admin123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void query_shouldReturnResultsForValidRequest() throws Exception {
        when(jwtService.validateToken("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("admin");
        when(elasticsearchService.search(anyString(), anyList()))
                .thenReturn(Map.of("position", List.of(
                        new Position("1", "AAPL", "100", "150.00")
                )));

        SearchRequest request = new SearchRequest(
                "valid-token", "admin", "AAPL",
                new SearchContext(List.of("Position")));

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position[0].security").value("AAPL"));
    }

    @Test
    void query_shouldReturnUnauthorizedForInvalidToken() throws Exception {
        when(jwtService.validateToken("invalid-token")).thenReturn(false);

        SearchRequest request = new SearchRequest(
                "invalid-token", "admin", "AAPL",
                new SearchContext(List.of("Position")));

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid or expired token"));
    }

    @Test
    void query_shouldReturnUnauthorizedWhenTokenUserMismatch() throws Exception {
        when(jwtService.validateToken("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("otheruser");

        SearchRequest request = new SearchRequest(
                "valid-token", "admin", "AAPL",
                new SearchContext(List.of("Position")));

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token does not match the provided user"));
    }

    @Test
    void query_shouldReturnBadRequestForQueryTooShort() throws Exception {
        SearchRequest request = new SearchRequest(
                "valid-token", "admin", "AB",
                new SearchContext(List.of("Position")));

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.query").value("Query must be between 3 and 15 characters"));
    }

    @Test
    void query_shouldReturnBadRequestForQueryTooLong() throws Exception {
        SearchRequest request = new SearchRequest(
                "valid-token", "admin", "1234567890123456",
                new SearchContext(List.of("Position")));

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.query").value("Query must be between 3 and 15 characters"));
    }

    @Test
    void query_shouldReturnBadRequestWhenQueryMissing() throws Exception {
        String json = "{\"token\":\"valid-token\",\"user\":\"admin\",\"context\":{\"index\":[\"Position\"]}}";

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void query_shouldReturnBadRequestWhenTokenMissing() throws Exception {
        String json = "{\"user\":\"admin\",\"query\":\"AAPL\",\"context\":{\"index\":[\"Position\"]}}";

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void query_shouldReturnServerErrorOnElasticsearchFailure() throws Exception {
        when(jwtService.validateToken("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("admin");
        when(elasticsearchService.search(anyString(), anyList()))
                .thenThrow(new IOException("Connection refused"));

        SearchRequest request = new SearchRequest(
                "valid-token", "admin", "AAPL",
                new SearchContext(List.of("Position")));

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void query_shouldAcceptQueryAtMinimumLength() throws Exception {
        when(jwtService.validateToken("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("admin");
        when(elasticsearchService.search(anyString(), anyList()))
                .thenReturn(Map.of("position", List.of()));

        SearchRequest request = new SearchRequest(
                "valid-token", "admin", "ABC",
                new SearchContext(List.of("Position")));

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void query_shouldAcceptQueryAtMaximumLength() throws Exception {
        when(jwtService.validateToken("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("admin");
        when(elasticsearchService.search(anyString(), anyList()))
                .thenReturn(Map.of("position", List.of()));

        SearchRequest request = new SearchRequest(
                "valid-token", "admin", "123456789012345",
                new SearchContext(List.of("Position")));

        mockMvc.perform(post("/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
