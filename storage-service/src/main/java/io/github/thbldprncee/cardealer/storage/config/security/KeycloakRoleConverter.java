package io.github.thbldprncee.cardealer.storage.config.security;

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
                .forEach(role -> addMappedAuthorities(authorities, role));

        return authorities;
    }

    private void addMappedAuthorities(List<GrantedAuthority> authorities, String keycloakRole) {
        addAuthority(authorities, keycloakRole);

        switch (keycloakRole) {
            case "CLIENT" -> addAuthority(authorities, "USER");
            case "SALES_MANAGER" -> addAuthority(authorities, "MANAGER");
            default -> {
            }
        }
    }

    private void addAuthority(List<GrantedAuthority> authorities, String role) {
        if (isSecurityRole(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
    }

    private boolean isSecurityRole(String role) {
        return switch (role) {
            case "CLIENT", "USER", "SALES_MANAGER", "MANAGER", "WAREHOUSE_ADMIN", "ADMIN" -> true;
            default -> false;
        };
    }
}
