package com.epam.trainerworkload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${security.jwt.secret-base64}") String secretBase64
    ) {
        byte[] decodedSecret = Base64.getDecoder().decode(secretBase64);

        if (decodedSecret.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes"
            );
        }

        return new SecretKeySpec(decodedSecret, "HmacSHA256");
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> standardValidator =
                JwtValidators.createDefaultWithIssuer("gym-crm");

        OAuth2TokenValidator<Jwt> audienceValidator =
                token -> {
                    if (token.getAudience().contains(
                            "trainer-workload-service"
                    )) {
                        return OAuth2TokenValidatorResult.success();
                    }

                    return OAuth2TokenValidatorResult.failure(
                            new OAuth2Error(
                                    "invalid_token",
                                    "Required audience is missing",
                                    null
                            )
                    );
                };

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        standardValidator,
                        audienceValidator
                )
        );

        return decoder;
    }
}
