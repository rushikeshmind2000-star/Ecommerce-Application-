package com.ecommerce.productservice.enums;

public enum ApiKey {
    success("success"),
    data("data"),
    message("message");

    private final String value;

    ApiKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
