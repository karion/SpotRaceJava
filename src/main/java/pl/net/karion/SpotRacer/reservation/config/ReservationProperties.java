package pl.net.karion.SpotRacer.reservation.config;

import java.time.LocalTime;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reservation")
public record ReservationProperties(
        LocalTime releaseAssignedSpotsAt,
        int standardWindowDays,
        int assignedWindowDays
) {
}