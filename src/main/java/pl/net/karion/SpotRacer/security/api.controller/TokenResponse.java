package pl.net.karion.SpotRacer.security.api.controller;

public record TokenResponse(
    String token,
    String type,
    String expiresAt
) {
}
