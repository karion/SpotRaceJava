package pl.net.karion.SpotRacer.security.api.controller;

public record LoginRequest(
        String email,
        String password
) {
}
