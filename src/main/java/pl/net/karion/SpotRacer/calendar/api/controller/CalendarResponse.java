package pl.net.karion.SpotRacer.calendar.api.controller;

import java.util.List;

public record CalendarResponse(
        List<CalendarDay> days
) {
    public CalendarResponse {
        days = List.copyOf(days);
    }
}
