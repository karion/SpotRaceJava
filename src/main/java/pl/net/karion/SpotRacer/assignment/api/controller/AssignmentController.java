package pl.net.karion.SpotRacer.assignment.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.assignment.service.AssignmentService;
import pl.net.karion.SpotRacer.spot.api.controller.LocationResponse;

import java.util.UUID;

@Tag(name = "Assignment", description = "Przypisywanie miejsc postojowych na stałe")
@RestController
@RequestMapping("/api/assignment")
public class AssignmentController {


    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse create(@Valid @RequestBody AssignmentCreateRequest request) {
        return this.assignmentService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(path = "/{id}")
    public AssignmentResponse getById(@PathVariable UUID id) {
        return this.assignmentService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AssignmentResponse update(@PathVariable UUID id, @Valid @RequestBody AssignmentUpdateRequest request) {
        return this.assignmentService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        this.assignmentService.delete(id);
    }

    // Do zastanowienia się czy to w ogóle jest potrzebne, czy nie ograniczyć się do podrządań spot/id i user/id
//    @PreAuthorize("hasRole('ADMIN')")
//    @SecurityRequirement(name = "bearerAuth")
//    @GetMapping
//    public Page<AssignmentResponse> getList(
//            @RequestParam(required = false) String search,
//            Pageable pageable
//    ) {
//        return this.assignmentService.getLocations(search, pageable);
//    }
}
