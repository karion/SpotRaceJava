package pl.net.karion.SpotRacer.spot.api.controller;

import jakarta.validation.constraints.NotBlank;

public record LocationRequest(
        @NotBlank
        String name,

        String description
) {
}
