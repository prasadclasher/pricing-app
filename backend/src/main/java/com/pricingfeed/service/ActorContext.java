package com.pricingfeed.service;

import com.pricingfeed.entity.UserRole;
import com.pricingfeed.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.pricingfeed.service.ApiExceptions.UnauthorizedException;

@Component
public class ActorContext {

    public AuthenticatedUser requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Authentication required");
        }
        return user;
    }

    public Actor requireActor() {
        AuthenticatedUser user = requirePrincipal();
        if (user.getRole() == UserRole.STORE_USER && user.getStoreId() == null) {
            throw new UnauthorizedException("Invalid user configuration");
        }
        return new Actor(user.getRole().name(), user.getUserId(), user.getStoreId());
    }

    public record Actor(String role, Long userId, Long storeId) {
        public boolean isHq() {
            return UserRole.HQ_USER.name().equals(role) || UserRole.HQ_ADMIN.name().equals(role);
        }
    }
}
