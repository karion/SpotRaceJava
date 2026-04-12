package pl.net.karion.SpotRacer.user.api.controller;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank
        String firstname,

        @NotBlank
        String lastname
) {
}
