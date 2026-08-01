package pl.net.karion.SpotRacer.calendar.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.calendar.service.CalendarService;
import pl.net.karion.SpotRacer.security.model.CurrentUser;
import pl.net.karion.SpotRacer.security.service.CurrentUserProvider;

@Tag(name = "Calendar", description = "Kalendarz z dostępnymi miejscami")
@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final CurrentUserProvider userProvider;

    public CalendarController(
        CalendarService calendarService,
        CurrentUserProvider userProvider
    ) {
        this.calendarService = calendarService;
        this.userProvider = userProvider;
    }

    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CalendarResponse getCalendar() {
        CurrentUser currentUser = this.userProvider.currentUser();
        return this.calendarService.getCalendar(currentUser.id());
    }
}
