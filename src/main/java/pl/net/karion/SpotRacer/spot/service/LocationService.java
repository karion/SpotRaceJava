package pl.net.karion.SpotRacer.spot.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.net.karion.SpotRacer.spot.api.controller.LocationRequest;
import pl.net.karion.SpotRacer.spot.api.controller.LocationResponse;
import pl.net.karion.SpotRacer.spot.exception.LocationNotFoundException;
import pl.net.karion.SpotRacer.spot.model.Location;
import pl.net.karion.SpotRacer.spot.model.LocationRepository;
import pl.net.karion.SpotRacer.user.api.controller.UserResponse;
import pl.net.karion.SpotRacer.user.exception.UserNotFoundException;
import pl.net.karion.SpotRacer.user.model.User;
import pl.net.karion.SpotRacer.user.service.UserMapper;
import pl.net.karion.SpotRacer.user.service.UserSpecifications;

import java.util.UUID;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public LocationResponse create(LocationRequest request) {
        Location location = new Location(
                UUID.randomUUID(),
                request.name(),
                request.description()
        );

        Location savedLocation = this.locationRepository.save(location);
        return LocationMapper.toResponse(savedLocation);
    }

    public LocationResponse update(LocationRequest request, UUID id) {
        Location location = this.locationRepository.findById(id)
                .orElseThrow(LocationNotFoundException::new);

        location.setName(request.name());
        location.setDescription(request.description());

        Location savedLocation = this.locationRepository.save(location);
        return LocationMapper.toResponse(savedLocation);
    }

    public LocationResponse getById(UUID id) {
        Location location = this.locationRepository.findById(id)
                .orElseThrow(LocationNotFoundException::new);
        return LocationMapper.toResponse(location);
    }


    public Page<LocationResponse> getLocations(String search, Pageable pageable) {
        Specification<Location> spec = Specification
                .where(LocationSpecifications.hasName(search))
                .or(LocationSpecifications.hasDescription(search));

        return this.locationRepository.findAll(spec, pageable)
                .map(LocationMapper::toResponse);
    }
}
