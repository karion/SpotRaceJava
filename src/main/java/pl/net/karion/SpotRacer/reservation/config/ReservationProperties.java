package pl.net.karion.SpotRacer.reservation.config;

import java.time.LocalTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "reservation")
@Validated
public record ReservationProperties(
    @NotNull
    LocalTime releaseAssignedSpotsAt,

    @Min(0)
    int standardWindowDays,

    @Min(0)
    int assignedWindowDays
) {

    @AssertTrue(message = "assignedWindowDays must be greater than or equal to standardWindowDays")
    public boolean isAssignedWindowGreaterOrEqualToStandardWindow() {
        return standardWindowDays <= assignedWindowDays;
    }
}