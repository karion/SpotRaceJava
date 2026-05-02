package pl.net.karion.SpotRacer.spot.fixtures;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.net.karion.SpotRacer.spot.model.Location;
import pl.net.karion.SpotRacer.spot.model.LocationRepository;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.spot.model.SpotRepository;

import java.util.UUID;

@Component
public class SpotFixture {

    @Autowired
    private LocationFixture locationFixture;

    private final SpotRepository spotRepository;

    public SpotFixture(SpotRepository spotRepository) {
        this.spotRepository = spotRepository;
    }
    public Spot createSpot() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String name = "Random spot-" + suffix;

        return this.createSpot(name);
    }

    public Spot createSpot(String name) {
        Location location = locationFixture.createLocation();
        Spot newSpot = new Spot(
                UUID.randomUUID(),
                name,
                location
        );

        return this.spotRepository.save(newSpot);
    }

    public Spot createSpot(String name, String locationName) {
        Location location = locationFixture.createLocation(locationName);
        Spot newSpot = new Spot(
                UUID.randomUUID(),
                name,
                location
        );

        return this.spotRepository.save(newSpot);
    }
}
