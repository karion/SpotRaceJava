package pl.net.karion.SpotRacer.reservation.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.reservation.service.ReservationService;

import java.util.UUID;

@Tag(name = "Reservation", description = "Rezerwowanie miejsc postojowych")
@RestController
@RequestMapping("/api/reservarion")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request) {
        return reservationService.create(request);
    }

    @DeleteMapping(path = "/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(UUID id) {
        this.reservationService.delete(id);
    }
}
