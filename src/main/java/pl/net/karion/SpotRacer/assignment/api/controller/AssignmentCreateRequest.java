package pl.net.karion.SpotRacer.assignment.api.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record AssignmentCreateRequest(
        @NotNull
        UUID userId,

        @NotNull
        UUID spotId,

        @NotNull
        @FutureOrPresent
        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 250)
        String note
) {

    @JsonIgnore
    @AssertTrue(message = "endDate must be >= startDate")
    public boolean isValidDateRange() {
        if (endDate == null) return true;
        return !endDate.isBefore(startDate);
    }
}
