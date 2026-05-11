package pl.net.karion.SpotRacer.assignment.api.controller;

import java.time.LocalDate;
import java.util.UUID;

public record AssignmentResponse(
        UUID id,
        UUID spotId,
        String spotName,
        UUID userId,
        String userName,
        LocalDate startDate,
        LocalDate endDate,
        String note

) {
}
