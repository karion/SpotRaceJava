package pl.net.karion.SpotRacer.calendar.api.controller;

import java.time.LocalDate;
import java.util.UUID;
import pl.net.karion.SpotRacer.calendar.model.SpotStatusEnum;

public record CalendarDayAvailability(
    LocalDate date,
    UUID spotId,
    String spotName,
    UUID locationId,
    String locationName,
    SpotStatusEnum status
){}
