package pl.net.karion.SpotRacer.reservation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.net.karion.SpotRacer.assignment.model.Assignment;
import pl.net.karion.SpotRacer.assignment.model.AssignmentRepository;
import pl.net.karion.SpotRacer.reservation.config.ReservationProperties;
import pl.net.karion.SpotRacer.reservation.exception.ReservationAlreadyTakenException;
import pl.net.karion.SpotRacer.reservation.exception.ReservationRequiresSelfOrAdminException;
import pl.net.karion.SpotRacer.reservation.exception.ReservationToFarInFutureException;
import pl.net.karion.SpotRacer.reservation.exception.ReservationWithAssignmentNotReleasedYetException;
import pl.net.karion.SpotRacer.reservation.model.ReservationRepository;
import pl.net.karion.SpotRacer.security.model.CurrentUser;
import pl.net.karion.SpotRacer.security.service.CurrentUserProvider;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.user.model.Role;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class ReservationAvailabilityServiceTest {

    @Mock
    Clock clock;

    @Mock
    ReservationProperties properties;

    @Mock
    AssignmentRepository assignmentRepository;

    @Mock
    CurrentUserProvider currentUserProvider;

    @Mock
    ReservationRepository reservationRepository;

    @InjectMocks
    ReservationAvailabilityService reservationAvailabilityService;

    @Test
    void shouldThrowExceptionIfNotSameUserOrAdmin() {

        UUID currentUserId = UUID.randomUUID();
        CurrentUser currentUser = new CurrentUser(currentUserId, EnumSet.of(Role.USER));

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(clock.instant()).thenReturn(Instant.parse("2026-05-24T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Janina",
                "Nowacka"
        );

        Spot spot = new Spot(
                UUID.randomUUID(),
                "Wolne",
                null
        );

        LocalDate today = LocalDate.now(clock);

        assertThrows(
                ReservationRequiresSelfOrAdminException.class,
                () -> reservationAvailabilityService.validateReservation(user, spot, today)
        );
    }

    @Test
    void shouldThrowConflictIfReservationExists() {
        UUID currentUserId = UUID.randomUUID();
        CurrentUser currentUser = new CurrentUser(currentUserId, EnumSet.of(Role.USER));

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(clock.instant()).thenReturn(Instant.parse("2026-05-24T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        User user = new User(
                currentUserId,
                "random@email.com",
                "",
                "Janina",
                "Nowacka"
        );

        Spot spot = new Spot(
                UUID.randomUUID(),
                "Wolne",
                null
        );

        LocalDate today = LocalDate.now(clock);

        when(reservationRepository.existsBySpotIdAndDate(spot.getId(), today))
                .thenReturn(true);

        assertThrows(
                ReservationAlreadyTakenException.class,
                () -> reservationAvailabilityService.validateReservation(user, spot, today)
        );
    }

    public static Stream<Arguments> dataForUserWithoutAssignment() {
        return Stream.of(
                Arguments.of(
                        "2026-05-24T10:00:00Z",
                        "2026-05-24",
                        null
                ),
                Arguments.of(
                        "2026-05-24T10:00:00Z",
                        "2026-05-25",
                        null
                ),
                Arguments.of(
                        "2026-05-24T10:00:00Z",
                        "2026-05-26",
                        ReservationToFarInFutureException.class
                )
        );
    }

    @ParameterizedTest
    @MethodSource("dataForUserWithoutAssignment")
    void shouldValidateReservationWindowForUserWithoutAssignment(
            String now,
            String reservationDateString,
            Class<? extends RuntimeException> exceptionClass
    ) {
        UUID currentUserId = UUID.randomUUID();
        CurrentUser currentUser = new CurrentUser(currentUserId, EnumSet.of(Role.USER));

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(clock.instant()).thenReturn(Instant.parse(now));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        User user = new User(
                currentUserId,
                "random@email.com",
                "",
                "Janina",
                "Nowacka"
        );

        Spot spot = new Spot(
                UUID.randomUUID(),
                "Wolne",
                null
        );

        LocalDate reservationDate = LocalDate.parse(reservationDateString);

        when(reservationRepository.existsBySpotIdAndDate(spot.getId(), reservationDate))
                .thenReturn(false);

        when(assignmentRepository.findActiveAssignment(spot.getId(), reservationDate))
                .thenReturn(Optional.empty());

        when(properties.standardWindowDays()).thenReturn(1);

        if (exceptionClass != null) {
            assertThrows(
                    exceptionClass,
                    () -> reservationAvailabilityService.validateReservation(user, spot, reservationDate)
            );
        } else {
            assertDoesNotThrow(
                    () -> reservationAvailabilityService.validateReservation(user, spot, reservationDate)
            );
        }
    }

    public static Stream<Arguments> dataForUserWithAssignment() {
        return Stream.of(
            Arguments.of(
                    "2026-05-20T10:00:00Z",
                    "2026-05-20",
                    null
            ),
            Arguments.of(
                    "2026-05-20T10:00:00Z",
                    "2026-05-21",
                    null
            ),
            Arguments.of(
                    "2026-05-20T10:00:00Z",
                    "2026-05-22",
                    null
            ),
            Arguments.of(
                    "2026-05-20T10:00:00Z",
                    "2026-05-27",
                    null
            ),
            Arguments.of(
                    "2026-05-20T10:00:00Z",
                    "2026-05-28",
                    ReservationToFarInFutureException.class
            )
        );
    }

    @ParameterizedTest
    @MethodSource("dataForUserWithAssignment")
    void shouldValidateReservationWindowForUserWithAssignment(
            String now,
            String reservationDateString,
            Class<? extends RuntimeException> exceptionClass
    ) {
        UUID currentUserId = UUID.randomUUID();
        CurrentUser currentUser = new CurrentUser(currentUserId, EnumSet.of(Role.USER));

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(clock.instant()).thenReturn(Instant.parse(now));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        User user = new User(
                currentUserId,
                "random@email.com",
                "",
                "Janina",
                "Nowacka"
        );

        Spot spot = new Spot(
                UUID.randomUUID(),
                "Wolne",
                null
        );

        Assignment assignment = new Assignment(
                UUID.randomUUID(),
                user,
                spot,
                LocalDate.parse("2026-05-10"),
                null,
                null
        );

        LocalDate reservationDate = LocalDate.parse(reservationDateString);

        when(reservationRepository.existsBySpotIdAndDate(spot.getId(), reservationDate))
                .thenReturn(false);

        when(assignmentRepository.findActiveAssignment(spot.getId(), reservationDate))
                .thenReturn(Optional.of(assignment));

        when(properties.assignedWindowDays()).thenReturn(7);

        if (exceptionClass != null) {
            assertThrows(
                    exceptionClass,
                    () -> reservationAvailabilityService.validateReservation(user, spot, reservationDate)
            );
        } else {
            assertDoesNotThrow(
                    () -> reservationAvailabilityService.validateReservation(user, spot, reservationDate)
            );
        }
    }

    public static Stream<Arguments> dataForUserWithAssignmentForOtherUser() {
        return Stream.of(
                Arguments.of(
                        "2026-05-20T07:01:00Z",
                        "2026-05-20",
                        null
                ),
                Arguments.of(
                        "2026-05-20T23:59:00Z",
                        "2026-05-20",
                        null
                ),
                Arguments.of(
                        "2026-05-20T07:00:00Z",
                        "2026-05-20",
                        ReservationWithAssignmentNotReleasedYetException.class
                )
        );
    }

    @ParameterizedTest
    @MethodSource("dataForUserWithAssignmentForOtherUser")
    void shouldValidateReservationWindowForUserWithAssignmentForOtherUser(
            String now,
            String reservationDateString,
            Class<? extends RuntimeException> exceptionClass
    ) {
        UUID currentUserId = UUID.randomUUID();
        CurrentUser currentUser = new CurrentUser(currentUserId, EnumSet.of(Role.USER));

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(clock.instant()).thenReturn(Instant.parse(now));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        User user = new User(
                currentUserId,
                "random@email.com",
                "",
                "Janina",
                "Nowacka"
        );

        User otherUser = new User(
                UUID.randomUUID(),
                "otherRandom@email.com",
                "",
                "Niedziela",
                "Sopocka"
        );

        Spot spot = new Spot(
                UUID.randomUUID(),
                "Wolne",
                null
        );

        Assignment assignment = new Assignment(
                UUID.randomUUID(),
                otherUser,
                spot,
                LocalDate.parse("2026-05-10"),
                null,
                null
        );

        LocalDate reservationDate = LocalDate.parse(reservationDateString);

        when(reservationRepository.existsBySpotIdAndDate(spot.getId(), reservationDate))
                .thenReturn(false);

        when(assignmentRepository.findActiveAssignment(spot.getId(), reservationDate))
                .thenReturn(Optional.of(assignment));

        when(properties.releaseAssignedSpotsAt()).thenReturn(LocalTime.parse("07:00"));

        if (exceptionClass != null) {
            assertThrows(
                    exceptionClass,
                    () -> reservationAvailabilityService.validateReservation(user, spot, reservationDate)
            );
        } else {
            assertDoesNotThrow(
                    () -> reservationAvailabilityService.validateReservation(user, spot, reservationDate)
            );
        }
    }

    @Test
    void shouldValidateFutureReservationWindowForUserWithAssignmentForOtherUser(
    ) {
        UUID currentUserId = UUID.randomUUID();
        CurrentUser currentUser = new CurrentUser(currentUserId, EnumSet.of(Role.USER));

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(clock.instant()).thenReturn(Instant.parse("2026-05-20T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        User user = new User(
                currentUserId,
                "random@email.com",
                "",
                "Janina",
                "Nowacka"
        );

        User otherUser = new User(
                UUID.randomUUID(),
                "otherRandom@email.com",
                "",
                "Niedziela",
                "Sopocka"
        );

        Spot spot = new Spot(
                UUID.randomUUID(),
                "Wolne",
                null
        );

        Assignment assignment = new Assignment(
                UUID.randomUUID(),
                otherUser,
                spot,
                LocalDate.parse("2026-05-10"),
                null,
                null
        );

        LocalDate reservationDate = LocalDate.parse("2026-05-21");

        when(reservationRepository.existsBySpotIdAndDate(spot.getId(), reservationDate))
                .thenReturn(false);

        when(assignmentRepository.findActiveAssignment(spot.getId(), reservationDate))
                .thenReturn(Optional.of(assignment));

        assertThrows(
            ReservationToFarInFutureException.class,
            () -> reservationAvailabilityService.validateReservation(user, spot, reservationDate)
        );
    }
}
