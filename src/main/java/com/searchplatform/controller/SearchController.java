package com.searchplatform.controller;

import com.searchplatform.model.LoginResponse;
import com.searchplatform.model.Position;
import com.searchplatform.model.SearchRequest;
import com.searchplatform.model.SearchResponse;
import com.searchplatform.service.AuthenticationService;
import com.searchplatform.service.ElasticsearchService;
import com.searchplatform.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final ElasticsearchService elasticsearchService;

    public SearchController(AuthenticationService authenticationService,
                            JwtService jwtService,
                            ElasticsearchService elasticsearchService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.elasticsearchService = elasticsearchService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestHeader("user") String user,
            @RequestHeader("password") String password) {

        if (!authenticationService.authenticate(user, password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/query")
    public ResponseEntity<?> query(@Valid @RequestBody SearchRequest searchRequest) {
        if (!jwtService.validateToken(searchRequest.getToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
        }

        String tokenUser = jwtService.extractUsername(searchRequest.getToken());
        if (!tokenUser.equals(searchRequest.getUser())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token does not match the provided user"));
        }

        try {
            Map<String, List<Position>> results = elasticsearchService.search(
                    searchRequest.getQuery(),
                    searchRequest.getContext().getIndex()
            );
            SearchResponse response = new SearchResponse(results);
            return ResponseEntity.ok(response.getResults());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to execute search: " + e.getMessage()));
        }
    }
}
