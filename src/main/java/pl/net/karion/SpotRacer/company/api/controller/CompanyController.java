package pl.net.karion.SpotRacer.company.api.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.company.service.CompanyService;
import pl.net.karion.SpotRacer.spot.api.controller.LocationResponse;

import java.util.UUID;

@Tag(name = "Company", description = "Zarządzanie firmami")
@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@Valid @RequestBody CompanyRequest request) {
        return this.companyService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyResponse getById(@PathVariable UUID id) {
        return this.companyService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Page<CompanyResponse> getList(
        @RequestParam(required = false) String search,
        Pageable pageable
    ) {
        return this.companyService.getAll(search, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyResponse update(@PathVariable UUID id, @Valid @RequestBody CompanyRequest request) {
        return this.companyService.update(id, request);
    }
}
