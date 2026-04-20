package pl.net.karion.SpotRacer.spot.api.controller;

import java.util.UUID;

public record SpotResponse(
        UUID id,
        String name,
        UUID locationId,
        String locationName
) {
}
