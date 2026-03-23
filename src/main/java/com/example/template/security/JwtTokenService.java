package com.example.template.security;

import com.example.template.config.ApplicationProperties;
import com.example.template.exception.AuthenticationException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class JwtTokenService {

    private final ApplicationProperties properties;

    public JwtTokenService(ApplicationProperties properties) {
        this.properties = properties;
    }

    public Mono<TokenBundle> issueTokens(UUID userId, String username) {
        var accessExpiry = OffsetDateTime.now(ZoneOffset.UTC).plus(properties.jwt().accessTokenTtl());
        var refreshExpiry = OffsetDateTime.now(ZoneOffset.UTC).plus(properties.jwt().refreshTokenTtl());
        return Mono.zip(
                sign(userId, username, "access", accessExpiry),
                sign(userId, username, "refresh", refreshExpiry)
        ).map(tuple -> new TokenBundle(tuple.getT1(), tuple.getT2(), accessExpiry, refreshExpiry));
    }

    public Mono<JwtClaims> verify(String token, String expectedType) {
        return Mono.fromCallable(() -> SignedJWT.parse(token))
                .flatMap(jwt -> Mono.fromCallable(() -> jwt.verify(new MACVerifier(secret()))
                                ? jwt.getJWTClaimsSet() : null))
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid token signature")))
                .map(claims -> new JwtClaims(
                        UUID.fromString(claims.getSubject()),
                        claims.getStringClaim("username"),
                        claims.getStringClaim("token_type"),
                        OffsetDateTime.ofInstant(claims.getExpirationTime().toInstant(), ZoneOffset.UTC)
                ))
                .onErrorMap(ParseException.class, ex -> new AuthenticationException("Malformed token"))
                .onErrorMap(JOSEException.class, ex -> new AuthenticationException("Token verification failed"))
                .flatMap(claims -> switch (claims.tokenType()) {
                    case "access", "refresh" -> Mono.just(claims);
                    default -> Mono.error(new AuthenticationException("Unsupported token type"));
                })
                .filter(claims -> claims.tokenType().equals(expectedType))
                .switchIfEmpty(Mono.error(new AuthenticationException("Unexpected token type")))
                .filter(claims -> claims.expiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC)))
                .switchIfEmpty(Mono.error(new AuthenticationException("Token expired")));
    }

    private Mono<String> sign(UUID userId, String username, String tokenType, OffsetDateTime expiresAt) {
        return Mono.fromCallable(() -> {
            var claimsSet = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .issuer(properties.jwt().issuer())
                    .audience(properties.jwt().audience())
                    .claim("username", username)
                    .claim("token_type", tokenType)
                    .issueTime(new Date())
                    .expirationTime(Date.from(expiresAt.toInstant()))
                    .build();
            var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            jwt.sign(new MACSigner(secret()));
            return jwt.serialize();
        }).onErrorMap(JOSEException.class, ex -> new IllegalStateException("Unable to sign JWT", ex));
    }

    private byte[] secret() {
        return properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
    }
}
