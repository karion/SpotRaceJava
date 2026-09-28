package pl.net.karion.SpotRacer.security.service;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import pl.net.karion.SpotRacer.security.model.UserDetails;
import pl.net.karion.SpotRacer.user.model.Role;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private static final Duration EXPIRATION_TIME = Duration.ofHours(1);
    private final JwtEncoder jwtEncoder;
    private final Clock clock;

    public JwtService(
            JwtEncoder jwtEncoder,
            Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
    }


    public JwtData generateToken(UserDetails userDetails) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(EXPIRATION_TIME).truncatedTo(ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spotracer")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(userDetails.getId().toString())
                .claim("email", userDetails.getEmail())
                .claim("roles", userDetails.getRoles().stream()
                        .map(Role::name)
                        .toList())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();

        return new JwtData(
            jwtEncoder
                .encode(JwtEncoderParameters.from(jwsHeader, claims))
                .getTokenValue(),
            expiresAt
        );
    }
}
