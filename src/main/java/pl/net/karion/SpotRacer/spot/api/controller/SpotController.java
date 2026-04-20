package pl.net.karion.SpotRacer.spot.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.spot.service.SpotService;

import java.util.UUID;

@Tag(name = "Spots", description = "Zarządzanie lokacjami i miejscami postojowymi")
@RestController
@RequestMapping("/api/spot")
public class SpotController {

    private SpotService spotService;

    public SpotController(SpotService spotService) {
        this.spotService = spotService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping()
    public SpotResponse create(@Valid @RequestBody SpotRequest request) {
        return this.spotService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(path = "/{id}")
    public SpotResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody SpotRequest request
    ) {

        return this.spotService.update(request, id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(path = "/{id}")
    public SpotResponse getById(@PathVariable UUID id) {
        return this.spotService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping()
    public Page<SpotResponse> getList(
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return this.spotService.getSpots(search, pageable);
    }
}
