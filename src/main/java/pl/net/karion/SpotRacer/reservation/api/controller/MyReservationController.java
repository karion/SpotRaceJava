package pl.net.karion.SpotRacer.reservation.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.reservation.service.ReservationService;
import pl.net.karion.SpotRacer.security.model.CurrentUser;
import pl.net.karion.SpotRacer.security.service.CurrentUserProvider;
import pl.net.karion.SpotRacer.spot.api.controller.LocationResponse;

@Tag(name = "Reservation")
@RestController
@RequestMapping("/api/me/reservations")
public class MyReservationController {

    private final ReservationService reservationService;
    private final CurrentUserProvider userProvider;

    public MyReservationController(
        ReservationService reservationService,
        CurrentUserProvider currentUserProvider
    ) {
        this.reservationService = reservationService;
        this.userProvider = currentUserProvider;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/current")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.OK)
    public Page<ReservationResponse> getMyReservations(
        Pageable pageable
    ) {
        CurrentUser currentUser = this.userProvider.currentUser();
        return reservationService.getUserReservations(currentUser.id(), pageable);
    }

}
