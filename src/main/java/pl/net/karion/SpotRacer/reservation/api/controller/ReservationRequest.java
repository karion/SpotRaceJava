package pl.net.karion.SpotRacer.reservation.api.controller;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationRequest(
    @NotNull
    UUID userId,

    @NotNull
    UUID spotId,

    @NotNull
    @FutureOrPresent
    LocalDate date
    ) {
}
