package io.github.thbldprncee.cardealer.storage.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID getCurrentUserId() {
        Jwt jwt = getCurrentJwt();

        String userId = jwt.getSubject();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("JWT claim sub is missing");
        }

        return UUID.fromString(userId);
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public static boolean isManager() {
        return hasRole("MANAGER");
    }

    public static boolean isWarehouseAdmin() {
        return hasRole("WAREHOUSE_ADMIN");
    }

    public static boolean isUser() {
        return hasRole("USER");
    }

    private static Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("Current authentication is not JWT");
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken();
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }

        throw new IllegalStateException("Current authentication is not JWT");
    }
}
