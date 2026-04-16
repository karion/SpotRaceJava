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


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {

    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;

    public JwtService(
            JwtDecoder jwtDecoder,
            JwtEncoder jwtEncoder
    ) {
        this.jwtDecoder = jwtDecoder;
        this.jwtEncoder = jwtEncoder;
    }


    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();

        List<Role> roles = userDetails.getRoles().stream()
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spotracer")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(userDetails.getId().toString())
                .claim("email", userDetails.getEmail())
                .claim("roles", roles.stream()
                        .map(Role::name)
                        .reduce((a, b) -> a + " " + b)
                        .orElse(""))
                .build();

        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(jwsHeader, claims))
                .getTokenValue();
    }
}
