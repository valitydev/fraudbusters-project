package dev.vality.fraudbusters.management.config.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String principleAttribute = "preferred_username";
    private static final String resourceAttribute = "resource_access";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(
                jwt,
                new HashSet<>(extractResourceRoles(jwt)),
                getPrincipleClaimName(jwt)
        );
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt token) {
        if (token.getClaim(resourceAttribute) == null) {
            return Set.of();
        }
        Map<String, Object> resourceAccess = token.getClaim(resourceAttribute);
        if (resourceAccess.isEmpty()) {
            return Set.of();
        }

        return resourceAccess.values().stream()
                .map(resourceAccessInfo -> (Map<String, Object>) resourceAccessInfo)
                .flatMap(resourceAccessInfo -> ((Collection<String>) resourceAccessInfo.get("roles")).stream())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    private String getPrincipleClaimName(Jwt jwt) {
        return jwt.getClaim(principleAttribute);
    }
}