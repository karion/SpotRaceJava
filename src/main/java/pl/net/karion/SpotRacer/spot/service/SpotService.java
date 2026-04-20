package pl.net.karion.SpotRacer.spot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.net.karion.SpotRacer.spot.api.controller.SpotRequest;
import pl.net.karion.SpotRacer.spot.api.controller.SpotResponse;
import pl.net.karion.SpotRacer.spot.exception.LocationNotFoundException;
import pl.net.karion.SpotRacer.spot.exception.SpotNotFoundException;
import pl.net.karion.SpotRacer.spot.model.Location;
import pl.net.karion.SpotRacer.spot.model.LocationRepository;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.spot.model.SpotRepository;

import java.util.UUID;

@Service
public class SpotService {

    private final SpotRepository spotRepository;
    private final LocationRepository locationRepository;

    public SpotService(
            SpotRepository spotRepository,
            LocationRepository locationRepository
    ) {
        this.spotRepository = spotRepository;
        this.locationRepository = locationRepository;
    }

    public SpotResponse create(SpotRequest request) {

        Location location = request.locationID() == null
                ? null
                : this.locationRepository.findById(request.locationID())
                  .orElseThrow(LocationNotFoundException::new);

        Spot spot = new Spot(
                UUID.randomUUID(),
                request.name(),
                location
        );

        Spot savedSpot = this.spotRepository.save(spot);
        return SpotMapper.toResponse(savedSpot);
    }

    public SpotResponse update(SpotRequest request, UUID id) {
        Spot spot = this.spotRepository.findById(id)
                .orElseThrow(SpotNotFoundException::new);

        Location location = request.locationID() == null
                ? null
                : this.locationRepository.findById(request.locationID())
                  .orElseThrow(LocationNotFoundException::new);

        spot.setName(request.name());
        spot.setLocation(location);

        Spot savedSpot = this.spotRepository.save(spot);
        return SpotMapper.toResponse(savedSpot);
    }

    public SpotResponse getById(UUID id) {
        Spot spot = this.spotRepository.findById(id)
                .orElseThrow(SpotNotFoundException::new);
        return SpotMapper.toResponse(spot);
    }

    public Page<SpotResponse> getSpots(String search, Pageable pageable) {
        Specification<Spot> spec = Specification
                .where(SpotSpecifications.hasName(search));

        return this.spotRepository.findAll(spec, pageable)
                .map(SpotMapper::toResponse);
    }
}
