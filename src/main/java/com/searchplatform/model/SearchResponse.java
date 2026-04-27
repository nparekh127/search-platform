package com.searchplatform.model;

import java.util.List;
import java.util.Map;

public class SearchResponse {

    private Map<String, List<Position>> results;

    public SearchResponse() {
    }

    public SearchResponse(Map<String, List<Position>> results) {
        this.results = results;
    }

    public Map<String, List<Position>> getResults() {
        return results;
    }

    public void setResults(Map<String, List<Position>> results) {
        this.results = results;
    }
}
