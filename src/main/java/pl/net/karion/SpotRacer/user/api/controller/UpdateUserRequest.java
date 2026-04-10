package pl.net.karion.SpotRacer.user.api.controller;

public record UpdateUserRequest(
        String firstname,
        String lastname
) {
}
