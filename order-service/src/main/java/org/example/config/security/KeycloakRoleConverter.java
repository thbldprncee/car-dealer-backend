package org.example.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return authorities;
        }

        Object rolesObject = realmAccess.get("roles");
        if (!(rolesObject instanceof Collection<?> roles)) {
            return authorities;
        }

        roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(this::mapRoleToTaskRole)
                .filter(this::isSecurityRole)
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        return authorities;
    }

    private String mapRoleToTaskRole(String keycloakRole) {
        return switch (keycloakRole) {
            case "CLIENT" -> "USER";
            case "SALES_MANAGER" -> "MANAGER";
            default -> keycloakRole;
        };
    }

    private boolean isSecurityRole(String role) {
        return switch (role) {
            case "USER", "MANAGER", "WAREHOUSE_ADMIN", "ADMIN" -> true;
            default -> false;
        };
    }
}
