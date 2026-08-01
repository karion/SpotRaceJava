package pl.net.karion.SpotRacer.reservation.model;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationKey(UUID spotId, LocalDate date) {
}
