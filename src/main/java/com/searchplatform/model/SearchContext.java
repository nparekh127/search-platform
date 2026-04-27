package com.searchplatform.model;

import java.util.List;

public class SearchContext {

    private List<String> index;

    public SearchContext() {
    }

    public SearchContext(List<String> index) {
        this.index = index;
    }

    public List<String> getIndex() {
        return index;
    }

    public void setIndex(List<String> index) {
        this.index = index;
    }
}
