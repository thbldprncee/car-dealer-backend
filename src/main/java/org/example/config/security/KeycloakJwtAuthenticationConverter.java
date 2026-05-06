package org.example.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess != null) {
            Object rolesObject = realmAccess.get("roles");

            if (rolesObject instanceof Collection<?> roles) {
                for (Object roleObject : roles) {
                    if (roleObject instanceof String role) {
                        addRole(authorities, role);
                    }
                }
            }
        }

        return new JwtAuthenticationToken(jwt, authorities, getPrincipalName(jwt));
    }

    private void addRole(Set<GrantedAuthority> authorities, String keycloakRole) {
        authorities.add(new SimpleGrantedAuthority("ROLE_" + keycloakRole));

        String mappedRole = mapRoleToTaskRole(keycloakRole);
        authorities.add(new SimpleGrantedAuthority("ROLE_" + mappedRole));
    }

    private String mapRoleToTaskRole(String keycloakRole) {
        return switch (keycloakRole) {
            case "CLIENT" -> "USER";
            case "SALES_MANAGER" -> "MANAGER";
            default -> keycloakRole;
        };
    }

    private String getPrincipalName(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");

        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        return jwt.getSubject();
    }
}