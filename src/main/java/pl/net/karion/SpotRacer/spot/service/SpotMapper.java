package pl.net.karion.SpotRacer.spot.service;

import pl.net.karion.SpotRacer.spot.api.controller.SpotResponse;
import pl.net.karion.SpotRacer.spot.model.Spot;

public class SpotMapper {

    public static SpotResponse toResponse(Spot  spot) {
        return new SpotResponse(
            spot.getId(),
            spot.getName(),
            spot.getLocation() != null ? spot.getLocation().getId() : null,
            spot.getLocation() != null ? spot.getLocation().getName() : null
        );
    }
}
