package pl.net.karion.SpotRacer.spot.api.controller;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SpotRequest(
        @NotBlank String name,
        UUID locationId
) {
}
