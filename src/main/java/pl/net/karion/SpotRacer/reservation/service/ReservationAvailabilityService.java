package pl.net.karion.SpotRacer.reservation.service;

import org.springframework.stereotype.Service;
import pl.net.karion.SpotRacer.assignment.model.Assignment;
import pl.net.karion.SpotRacer.assignment.model.AssignmentRepository;
import pl.net.karion.SpotRacer.reservation.config.ReservationProperties;
import pl.net.karion.SpotRacer.reservation.exception.*;
import pl.net.karion.SpotRacer.reservation.model.ReservationRepository;
import pl.net.karion.SpotRacer.security.model.CurrentUser;
import pl.net.karion.SpotRacer.security.service.CurrentUserProvider;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.user.model.Role;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class ReservationAvailabilityService {
    private final Clock clock;
    private final ReservationProperties properties;
    private final AssignmentRepository assignmentRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ReservationRepository reservationRepository;

    public ReservationAvailabilityService(
            Clock clock,
            ReservationProperties properties,
            AssignmentRepository assignmentRepository,
            CurrentUserProvider currentUserProvider,
            ReservationRepository  reservationRepository
    ) {
        this.clock = clock;
        this.properties = properties;
        this.assignmentRepository = assignmentRepository;
        this.currentUserProvider = currentUserProvider;
        this.reservationRepository = reservationRepository;
    }

    void validateReservation(
            User user,
            Spot spot,
            LocalDate reservationDate
    ) {
        CurrentUser currentUser = this.currentUserProvider.currentUser();

        if (!user.getId().equals(currentUser.id())) {
            if (!currentUser.hasRole(Role.ADMIN)) {
                throw new ReservationRequiresSelfOrAdminException();
            }
        }

        if (this.reservationRepository.existsBySpotIdAndDate(spot.getId(), reservationDate)) {
            throw new ReservationAlreadyTakenException();
        }

        Assignment assignment = this.assignmentRepository.findActiveAssignment(spot.getId(), reservationDate)
                .orElse(null);

        if (assignment == null) {
            if (!this.isFromTodayToSomeDay(reservationDate, this.properties.standardWindowDays())) {
                throw new ReservationTooFarInFutureException();
            }

            return;
        }

        if (assignment.getUser().getId().equals(currentUser.id())) {
            if (!this.isFromTodayToSomeDay(reservationDate, this.properties.assignedWindowDays())) {
                throw new ReservationTooFarInFutureException();
            }
        } else {
            if (!this.isTodayAfterReleaseTime(reservationDate)) {
                throw new ReservationWithAssignmentNotReleasedYetException();
            }
        }
    }

    private boolean isFromTodayToSomeDay(LocalDate reservationDate, int days) {
        LocalDate today = LocalDate.now(clock);
        LocalDate futureDay =  today.plusDays(days);

        return !reservationDate.isBefore(today) && !reservationDate.isAfter(futureDay);
    }

    private boolean isTodayAfterReleaseTime(LocalDate reservationDate) {
        LocalDate today = LocalDate.now(clock);

        if (!reservationDate.isEqual(today)) {
            throw new ReservationTooFarInFutureException();
        }

        LocalTime now = LocalTime.now(clock);
        return !now.isBefore(this.properties.releaseAssignedSpotsAt());
    }
}
