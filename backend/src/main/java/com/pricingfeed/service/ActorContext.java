package com.pricingfeed.service;

import org.springframework.stereotype.Component;

import static com.pricingfeed.service.ApiExceptions.BadRequestException;

@Component
public class ActorContext {
    public Actor actor(String role, Long userId, Long storeId) {
        if (role == null || userId == null) {
            throw new BadRequestException("x-role and x-user-id headers are required");
        }
        if ("STORE_USER".equalsIgnoreCase(role) && storeId == null) {
            throw new BadRequestException("x-store-id header is required for STORE_USER");
        }
        return new Actor(role.toUpperCase(), userId, storeId);
    }

    public record Actor(String role, Long userId, Long storeId) {
        public boolean isHq() { return "HQ_USER".equals(role) || "HQ_ADMIN".equals(role); }
    }
}
