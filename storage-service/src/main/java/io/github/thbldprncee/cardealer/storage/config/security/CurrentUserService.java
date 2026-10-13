package io.github.thbldprncee.cardealer.storage.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
public class CurrentUserService {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("Current user is not authenticated by JWT");
        }

        Jwt jwt = extractJwt(authentication);
        String userId = jwt.getSubject();

        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("JWT claim sub is missing");
        }

        return UUID.fromString(userId);
    }

    private Jwt extractJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken();
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }

        throw new IllegalStateException("Current user is not authenticated by JWT");
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    public boolean hasAnyRole(String... roles) {
        return Arrays.stream(roles).anyMatch(this::hasRole);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isManager() {
        return hasRole("MANAGER");
    }

    public boolean isWarehouseAdmin() {
        return hasRole("WAREHOUSE_ADMIN");
    }

    public boolean isUser() {
        return hasRole("USER");
    }
}
