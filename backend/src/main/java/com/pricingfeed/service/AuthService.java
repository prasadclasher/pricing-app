package com.pricingfeed.service;

import com.pricingfeed.api.request.LoginRequest;
import com.pricingfeed.api.response.LoginResponse;
import com.pricingfeed.api.response.MeResponse;
import com.pricingfeed.entity.AppUser;
import com.pricingfeed.entity.UserRole;
import com.pricingfeed.repo.AppUserRepository;
import com.pricingfeed.security.AuthenticatedUser;
import com.pricingfeed.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.pricingfeed.service.ApiExceptions.UnauthorizedException;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ActorContext actorContext;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                      ActorContext actorContext) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.actorContext = actorContext;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        validateUserRoleStore(user);
        return new LoginResponse(jwtService.createToken(user.getId()), jwtService.getExpirationSeconds());
    }

    public MeResponse me() {
        AuthenticatedUser user = actorContext.requirePrincipal();
        return new MeResponse(user.getUserId(), user.getUsername(), user.getRole().name(), user.getStoreId());
    }

    private void validateUserRoleStore(AppUser user) {
        if (user.getRole() == UserRole.STORE_USER && user.getStoreId() == null) {
            throw new UnauthorizedException("Invalid user configuration");
        }
        if ((user.getRole() == UserRole.HQ_ADMIN || user.getRole() == UserRole.HQ_USER) && user.getStoreId() != null) {
            throw new UnauthorizedException("Invalid user configuration");
        }
    }
}
