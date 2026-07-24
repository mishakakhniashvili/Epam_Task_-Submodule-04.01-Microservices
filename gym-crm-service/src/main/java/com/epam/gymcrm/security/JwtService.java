package com.epam.gymcrm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final Duration tokenLifetime;
    private final Duration serviceTokenLifetime;
    private final Clock clock;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.expiration-minutes}")
            long expirationMinutes,
            @Value("${security.jwt.service-token-expiration-minutes}")
            long serviceTokenExpirationMinutes,
            Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.tokenLifetime = Duration.ofMinutes(expirationMinutes);
        this.serviceTokenLifetime =
                Duration.ofMinutes(serviceTokenExpirationMinutes);
        this.clock = clock;
    }

    public String generateToken(Authentication authentication) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(tokenLifetime);

        String authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gym-crm")
                .subject(authentication.getName())
                .audience(List.of(
                        "gym-crm-service",
                        "trainer-workload-service"
                ))
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("scope", authorities)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    public String generateWorkloadServiceToken() {
        Instant issuedAt = clock.instant();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gym-crm")
                .subject("gym-crm-service")
                .audience(List.of("trainer-workload-service"))
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(serviceTokenLifetime))
                .claim("scope", "workload.write")
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    public long getExpirationSeconds() {
        return tokenLifetime.toSeconds();
    }
}
