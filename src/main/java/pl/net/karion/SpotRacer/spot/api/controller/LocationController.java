package pl.net.karion.SpotRacer.spot.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.spot.service.LocationService;

import java.util.UUID;

@Tag(name = "Spots", description = "Zarządzanie lokacjami i miejscami postojowymi")
@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public LocationResponse create(@Valid @RequestBody LocationRequest request) {
        return this.locationService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(path = "/{id}")
    public LocationResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody LocationRequest request
    ) {

        return this.locationService.update(request, id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(path = "/{id}")
    public LocationResponse getById(@PathVariable UUID id) {
        return this.locationService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public Page<LocationResponse> getList(
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return this.locationService.getLocations(search, pageable);
    }
}
