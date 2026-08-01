package pl.net.karion.SpotRacer.calendar.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.net.karion.SpotRacer.assignment.model.Assignment;
import pl.net.karion.SpotRacer.assignment.model.AssignmentRepository;
import pl.net.karion.SpotRacer.calendar.api.controller.CalendarDay;
import pl.net.karion.SpotRacer.calendar.api.controller.CalendarDayAvailability;
import pl.net.karion.SpotRacer.calendar.api.controller.CalendarResponse;
import pl.net.karion.SpotRacer.calendar.model.SpotStatusEnum;
import pl.net.karion.SpotRacer.reservation.config.ReservationProperties;
import pl.net.karion.SpotRacer.reservation.model.Reservation;
import pl.net.karion.SpotRacer.reservation.model.ReservationKey;
import pl.net.karion.SpotRacer.reservation.model.ReservationRepository;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.spot.model.SpotRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final SpotRepository spotRepository;
    private final AssignmentRepository assignmentRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;
    private final ReservationProperties reservationProperties;


    public CalendarService(
            SpotRepository spotRepository,
            AssignmentRepository assignmentRepository,
            ReservationRepository reservationRepository,
            Clock clock,
            ReservationProperties reservationProperties
    ) {
        this.spotRepository = spotRepository;
        this.assignmentRepository = assignmentRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
        this.reservationProperties = reservationProperties;
    }

    public CalendarResponse getCalendar(UUID userId) {

        List<CalendarDay> days = new ArrayList<>();

        ZonedDateTime now = ZonedDateTime.now(clock);

        LocalDate today = now.toLocalDate();
        LocalDate standardWindowEnd = today.plusDays(reservationProperties.standardWindowDays());
        LocalDate assignedWindowEnd = today.plusDays(reservationProperties.assignedWindowDays());

        List<Assignment> assignments = this.assignmentRepository.findActiveAssignmentsBetween(
                today,
                assignedWindowEnd
        );

        List<Assignment> userAssignments = assignments
                .stream()
                .filter(assignment -> assignment.getUser().getId().equals(userId))
                .toList();

        boolean isLongRange = !userAssignments.isEmpty();

        Map<UUID, List<Assignment>> assignmentsBySpotId = assignments.stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getSpot().getId()
                ));

        List<Spot> spots = this.spotRepository.findAll(
                Sort.by(
                    Sort.Order.asc("location.name").nullsLast(),
                    Sort.Order.asc("name").nullsLast()
                )
        );

        List<Reservation> reservations = this.reservationRepository.findReservationsBetween(today, assignedWindowEnd);
        Map<ReservationKey, Reservation> reservationsBySpotAndDate =
                reservations.stream()
                        .collect(Collectors.toMap(
                                reservation -> new ReservationKey(
                                        reservation.getSpot().getId(),
                                        reservation.getDate()
                                ),
                                Function.identity()
                        ));
        LocalDate rangeEnd = (isLongRange ? assignedWindowEnd: standardWindowEnd).plusDays(1);

        today.datesUntil(rangeEnd)
            .forEach(date -> {
                CalendarDay day = this.createDay(
                        date,
                        spots,
                        reservationsBySpotAndDate,
                        assignmentsBySpotId,
                        userId,
                        !date.isAfter(standardWindowEnd),
                        now
                );
                days.add(day);
            });

        return new CalendarResponse(days);
    }

    private CalendarDay createDay(
            LocalDate date,
            List<Spot> spots,
            Map<ReservationKey, Reservation> reservationsBySpotAndDate,
            Map<UUID, List<Assignment>> assignmentsBySpotId,
            UUID userId,
            boolean isStandardWindow,
            ZonedDateTime now
    ) {

        List<CalendarDayAvailability> availabilities = new ArrayList<>();

        for (Spot spot : spots) {
            List<Assignment> spotAssignments = assignmentsBySpotId
                .getOrDefault(
                    spot.getId(),
                    List.of()
                );

            SpotStatusEnum status = resolveStatus(
                    date,
                    reservationsBySpotAndDate.get(new ReservationKey(spot.getId(), date)),
                    spotAssignments
                        .stream()
                        .filter( a -> a.isForThisDate(date))
                        .findFirst()
                        .orElse(null),
                    userId,
                    isStandardWindow,
                    now
            );

            CalendarDayAvailability availability = new CalendarDayAvailability(
                    date,
                    spot.getId(),
                    spot.getName(),
                    spot.getLocation() != null ? spot.getLocation().getId() : null,
                    spot.getLocation() != null ? spot.getLocation().getName() : null,
                    status
            );

            availabilities.add(availability);
        }

        return new CalendarDay(
                date,
                availabilities
        );
    }

    private SpotStatusEnum resolveStatus(
            LocalDate date,
            Reservation reservation,
            Assignment assignment,
            UUID userId,
            boolean forStandardWindow,
            ZonedDateTime now
    ) {
        if (reservation != null) {
            return reservation.getUser().getId().equals(userId) ?
                SpotStatusEnum.RESERVED_TO_YOU :
                SpotStatusEnum.RESERVED
            ;
        }

        // przypisanie
        if (assignment != null) {

            LocalDate today = now.toLocalDate();
            if (today.equals(date)) {
                LocalTime currentTime = now.toLocalTime();
                if (currentTime.isAfter(this.reservationProperties.releaseAssignedSpotsAt())) {
                    return SpotStatusEnum.FREE;
                }
            }

            return assignment.getUser().getId().equals(userId)?
                SpotStatusEnum.ASSIGNED_TO_YOU:
                SpotStatusEnum.ASSIGNED_TO_OTHER
            ;
        }

        // brak rezerwacji i przypisania miejsca - wolne
        return forStandardWindow ?
            SpotStatusEnum.FREE:
            SpotStatusEnum.NOT_OPEN_YET;
    }
}
