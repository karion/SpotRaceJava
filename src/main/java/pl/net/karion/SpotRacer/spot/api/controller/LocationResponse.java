package pl.net.karion.SpotRacer.spot.api.controller;

import java.util.UUID;

public record LocationResponse(
        UUID id,
        String name,
        String description
) {
}
