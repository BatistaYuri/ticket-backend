package br.com.yuri.ticketbackend.security.controller;

import br.com.yuri.ticketbackend.security.dto.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private static final Duration TOKEN_DURATION =
            Duration.ofHours(8);

    private final JwtEncoder jwtEncoder;

    public AuthenticationController(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/manager-token")
    public ResponseEntity<TokenResponse> generateManagerToken() {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(TOKEN_DURATION);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("manager")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        String token = jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                )
                .getTokenValue();

        return ResponseEntity.ok(
                new TokenResponse(token)
        );
    }
}