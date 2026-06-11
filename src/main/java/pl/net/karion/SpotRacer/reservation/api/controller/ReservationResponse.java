package pl.net.karion.SpotRacer.reservation.api.controller;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID userId,
        String userName,
        UUID spotId,
        String spotName,
        LocalDate date
) {
}
