package com.pricingfeed.api.response;

public class MeResponse {
    private final Long userId;
    private final String username;
    private final String role;
    private final Long storeId;

    public MeResponse(Long userId, String username, String role, Long storeId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.storeId = storeId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Long getStoreId() {
        return storeId;
    }
}
