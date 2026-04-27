package com.searchplatform.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Position {

    private String id;
    private String security;
    private String qty;

    @JsonProperty("unit_price")
    private String unitPrice;

    public Position() {
    }

    public Position(String id, String security, String qty, String unitPrice) {
        this.id = id;
        this.security = security;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }
}
