package pl.net.karion.SpotRacer.security.service;

import java.time.Instant;

public record JwtData(
    String jwt,
    Instant expiresAt
) {
}
