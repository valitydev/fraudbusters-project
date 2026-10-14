package dev.vality.fraudbusters.management.utils;

import dev.vality.fraudbusters.management.config.converter.JwtAuthConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final JwtAuthConverter jwtAuthConverter;

    public static final String UNKNOWN = "UNKNOWN";

    public String getUserName() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("authentication: {}", authentication);
        if (authentication == null || authentication.getPrincipal() == null) {
            return UNKNOWN;
        }
        return jwtAuthConverter.convert((org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal())
                .getName();
    }
}
