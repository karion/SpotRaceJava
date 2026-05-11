package pl.net.karion.SpotRacer.assignment.api.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record AssignmentUpdateRequest(
        @NotNull
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
