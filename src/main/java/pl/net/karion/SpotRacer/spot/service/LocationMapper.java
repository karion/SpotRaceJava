package pl.net.karion.SpotRacer.spot.service;

import pl.net.karion.SpotRacer.spot.api.controller.LocationResponse;
import pl.net.karion.SpotRacer.spot.model.Location;

public class LocationMapper {

    public static LocationResponse toResponse(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getDescription()
        );
    }
}
