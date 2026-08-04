package pl.net.karion.SpotRacer.calendar.api.controller;

import java.time.LocalDate;
import java.util.List;

public record CalendarDay(
        LocalDate date,
        List<CalendarDayAvailability> availabilities
) {
    public CalendarDay {
        availabilities = List.copyOf(availabilities);
    }
}
