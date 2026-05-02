package pl.net.karion.SpotRacer.spot.fixtures;

import org.springframework.stereotype.Component;
import pl.net.karion.SpotRacer.spot.model.Location;
import pl.net.karion.SpotRacer.spot.model.LocationRepository;

import java.util.UUID;

@Component
public class LocationFixture {
    private final LocationRepository locationRepository;

    public LocationFixture(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location createLocation() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String name = "Random location-" + suffix;

        return this.createLocation(name);
    }

    public Location createLocation(String name) {
        Location newLocation = new Location(
                UUID.randomUUID(),
                name,
                "Not so random description"
        );

        return this.locationRepository.save(newLocation);
    }
}
