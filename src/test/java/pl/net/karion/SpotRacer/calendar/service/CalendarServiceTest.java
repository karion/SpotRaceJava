package pl.net.karion.SpotRacer.calendar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import pl.net.karion.SpotRacer.assignment.model.Assignment;
import pl.net.karion.SpotRacer.assignment.model.AssignmentRepository;
import pl.net.karion.SpotRacer.calendar.api.controller.CalendarDay;
import pl.net.karion.SpotRacer.calendar.api.controller.CalendarResponse;
import pl.net.karion.SpotRacer.calendar.model.SpotStatusEnum;
import pl.net.karion.SpotRacer.reservation.config.ReservationProperties;
import pl.net.karion.SpotRacer.reservation.model.Reservation;
import pl.net.karion.SpotRacer.reservation.model.ReservationRepository;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.spot.model.SpotRepository;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationProperties reservationProperties;

    private CalendarService calendarService;

    private static final ZoneId APP_ZONE = ZoneId.of("Europe/Warsaw");

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
            ZonedDateTime.of(
                2026, 7, 31,
                12, 0, 0, 0,
                APP_ZONE
            ).toInstant(),
            APP_ZONE
        );

        calendarService = new CalendarService(
            spotRepository,
            assignmentRepository,
            reservationRepository,
            clock,
            reservationProperties
        );
    }

    @Nested
    class GetCalendar {

        @Test
        void shouldReturnStandardWindowWhenUserHasNoAssignments() {
            UUID userId = UUID.randomUUID();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            when(assignmentRepository.findActiveAssignmentsBetween(
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 8, 7)
            )).thenReturn(List.of());

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of());

            when(reservationRepository.findReservationsBetween(
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 8, 7)
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(2);
            assertThat(response.days())
                .allSatisfy(day -> assertThat(day.availabilities()).isEmpty());
        }

        @Test
        void shouldReturnAssignedWindowWhenUserHasAssignmentInAssignedRange() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);
            Assignment assignment = new Assignment(
                UUID.randomUUID(),
                user,
                spot,
                LocalDate.of(2026, 7, 30),
                null,
                null
            );

            when(assignmentRepository.findActiveAssignmentsBetween(
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 8, 7)
            )).thenReturn(List.of(assignment));

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of());

            when(reservationRepository.findReservationsBetween(
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 8, 7)
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(8);
            assertThat(response.days())
                .extracting(CalendarDay::date)
                .containsExactly(
                    LocalDate.of(2026, 7, 31),
                    LocalDate.of(2026, 8, 1),
                    LocalDate.of(2026, 8, 2),
                    LocalDate.of(2026, 8, 3),
                    LocalDate.of(2026, 8, 4),
                    LocalDate.of(2026, 8, 5),
                    LocalDate.of(2026, 8, 6),
                    LocalDate.of(2026, 8, 7)
                );
        }

        @Test
        void shouldFetchAssignmentsForWholeAssignedWindow() {
            UUID userId = UUID.randomUUID();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of());

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            calendarService.getCalendar(userId);

            verify(assignmentRepository).findActiveAssignmentsBetween(
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 8, 7)
            );
        }

        @Test
        void shouldFetchReservationsForWholeAssignedWindow() {
            UUID userId = UUID.randomUUID();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of());

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            calendarService.getCalendar(userId);

            verify(reservationRepository).findReservationsBetween(
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 8, 7)
            );
        }

        @Test
        void shouldFetchSpotsSortedByLocationNameAndSpotName() {
            UUID userId = UUID.randomUUID();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of());

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            calendarService.getCalendar(userId);

            ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
            verify(spotRepository).findAll(sortCaptor.capture());

            Sort sort = sortCaptor.getValue();

            assertThat(sort.getOrderFor("location.name")).isNotNull();
            assertThat(sort.getOrderFor("location.name").getDirection()).isEqualTo(Sort.Direction.ASC);
            assertThat(sort.getOrderFor("location.name").getNullHandling()).isEqualTo(Sort.NullHandling.NULLS_LAST);

            assertThat(sort.getOrderFor("name")).isNotNull();
            assertThat(sort.getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
            assertThat(sort.getOrderFor("name").getNullHandling()).isEqualTo(Sort.NullHandling.NULLS_LAST);
        }

        @Test
        void shouldReturnDaysStartingFromToday() {
            UUID userId = UUID.randomUUID();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of());

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(2);
            assertThat(response.days().getFirst().date())
                .isEqualTo(LocalDate.of(2026, 7, 31));
        }

        @Test
        void shouldReturnDatesUpToStandardWindowWhenUserHasNoAssignments() {
            UUID userId = UUID.randomUUID();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of());

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(2);
            assertThat(response.days().getLast().date())
                .isEqualTo(LocalDate.of(2026, 8, 1));
        }
    }

    @Nested
    class ReservationStatus {

        @Test
        void shouldMarkSpotAsReservedToYouWhenReservationBelongsToCurrentUser() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            Reservation reservation = new Reservation(
                UUID.randomUUID(),
                user,
                spot,
                LocalDate.of(2026, 7, 31)
            );
            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of(reservation));

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(2);
            assertThat(response.days().getFirst().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.RESERVED_TO_YOU.name());
        }

        @Test
        void shouldMarkSpotAsReservedWhenReservationBelongsToOtherUser() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );

            UUID userId = user.getId();

            User otherUser = new User(
                UUID.randomUUID(),
                "other.user@email.com",
                "",
                "Oliwia",
                "Kowalski"
            );

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            Reservation reservation = new Reservation(
                UUID.randomUUID(),
                otherUser,
                spot,
                LocalDate.of(2026, 7, 31)
            );
            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of(reservation));

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(2);
            assertThat(response.days().getFirst().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.RESERVED.name());
        }

        @Test
        void shouldPreferReservationOverAssignment() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            Assignment assignment = new Assignment(
                UUID.randomUUID(),
                user,
                spot,
                LocalDate.of(2026, 7, 31),
                null,
                null
            );

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of(assignment));

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            Reservation reservation = new Reservation(
                UUID.randomUUID(),
                user,
                spot,
                LocalDate.of(2026, 7, 31)
            );
            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of(reservation));

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(8);
            assertThat(response.days().getFirst().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.RESERVED_TO_YOU.name());
            assertThat(response.days().getLast().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.ASSIGNED_TO_YOU.name());
        }
    }

    @Nested
    class AssignmentStatus {

        @Test
        void shouldMarkSpotAsAssignedToYouWhenAssignmentBelongsToCurrentUser() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);
            when(reservationProperties.releaseAssignedSpotsAt()).thenReturn(LocalTime.parse("12:01"));

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            Assignment assignment = new Assignment(
                UUID.randomUUID(),
                user,
                spot,
                LocalDate.of(2026, 7, 31),
                null,
                null
            );

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of(assignment));

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(8);
            assertThat(response.days().getFirst().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.ASSIGNED_TO_YOU.name());
            assertThat(response.days().getLast().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.ASSIGNED_TO_YOU.name());
        }

        @Test
        void shouldMarkSpotAsAssignedToOtherWhenAssignmentBelongsToOtherUser() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );
            User otherUser = new User(
                UUID.randomUUID(),
                "other.user@email.com",
                "",
                "Olga",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);
            when(reservationProperties.releaseAssignedSpotsAt()).thenReturn(LocalTime.parse("12:01"));

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            Assignment assignment = new Assignment(
                UUID.randomUUID(),
                otherUser,
                spot,
                LocalDate.of(2026, 7, 31),
                null,
                null
            );

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of(assignment));

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(2);
            assertThat(response.days().getFirst().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.ASSIGNED_TO_OTHER.name());
            assertThat(response.days().getLast().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.ASSIGNED_TO_OTHER.name());
        }

        @Test
        void shouldMarkAssignedSpotAsFreeWhenTodayAndReleaseTimeHasPassed() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );
            User otherUser = new User(
                UUID.randomUUID(),
                "other.user@email.com",
                "",
                "Olga",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);
            when(reservationProperties.releaseAssignedSpotsAt()).thenReturn(LocalTime.parse("08:00"));

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            Assignment assignment = new Assignment(
                UUID.randomUUID(),
                otherUser,
                spot,
                LocalDate.of(2026, 7, 31),
                null,
                null
            );

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of(assignment));

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(2);
            assertThat(response.days().getFirst().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.FREE.name());
        }

        @Test
        void shouldNotReleaseAssignedSpotForFutureDateEvenWhenReleaseTimeHasPassed() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );
            User otherUser = new User(
                UUID.randomUUID(),
                "other.user@email.com",
                "",
                "Olga",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);
            when(reservationProperties.releaseAssignedSpotsAt()).thenReturn(LocalTime.parse("08:00"));

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            Assignment assignment = new Assignment(
                UUID.randomUUID(),
                otherUser,
                spot,
                LocalDate.of(2026, 7, 31),
                null,
                null
            );

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of(assignment));

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(2);
            assertThat(response.days().getFirst().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.FREE.name());
            assertThat(response.days().get(1).availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.ASSIGNED_TO_OTHER.name());
        }

        @Test
        void shouldUseAssignmentOnlyWhenItIsActiveForDate() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );
            User otherUser = new User(
                UUID.randomUUID(),
                "other.user@email.com",
                "",
                "Olga",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);
            when(reservationProperties.releaseAssignedSpotsAt()).thenReturn(LocalTime.parse("12:01"));

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            Assignment assignment = new Assignment(
                UUID.randomUUID(),
                otherUser,
                spot,
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 7, 31),
                null
            );

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of(assignment));

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(2);
            assertThat(response.days().getFirst().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.ASSIGNED_TO_OTHER.name());
            assertThat(response.days().getLast().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.FREE.name());
        }
    }

    @Nested
    class FreeAndNotOpenYetStatus {

        @Test
        void shouldMarkSpotAsFreeWhenNoReservationAndNoAssignmentInStandardWindow() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);
            when(reservationProperties.releaseAssignedSpotsAt()).thenReturn(LocalTime.parse("12:01"));

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            Assignment assignment = new Assignment(
                UUID.randomUUID(),
                user,
                spot,
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 7, 31),
                null
            );

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of(assignment));

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSize(8);
            assertThat(response.days().getFirst().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.ASSIGNED_TO_YOU.name());
            assertThat(response.days().getLast().availabilities().getFirst().status().name())
                .isEqualTo(SpotStatusEnum.NOT_OPEN_YET.name());
        }

        @Test
        void shouldMarkSpotAsNotOpenYetWhenNoReservationAndNoAssignmentOutsideStandardWindow() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);
            when(reservationProperties.releaseAssignedSpotsAt()).thenReturn(LocalTime.parse("12:00"));

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            Assignment assignment = new Assignment(
                UUID.randomUUID(),
                user,
                spot,
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 7, 31),
                null
            );

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of(assignment));

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSizeGreaterThanOrEqualTo(8);
            assertThat(response.days().subList(2, 8))
                .allSatisfy(day -> assertThat(day.availabilities())
                    .allSatisfy(availability -> assertThat(availability.status())
                        .isEqualTo(SpotStatusEnum.NOT_OPEN_YET)));
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void shouldHandleSpotWithoutAssignments() {
            User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
            );

            UUID userId = user.getId();

            when(reservationProperties.standardWindowDays()).thenReturn(1);
            when(reservationProperties.assignedWindowDays()).thenReturn(7);

            Spot spot = new Spot(UUID.randomUUID(), "Spot1", null);

            when(assignmentRepository.findActiveAssignmentsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            when(spotRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(spot));

            when(reservationRepository.findReservationsBetween(
                any(),
                any()
            )).thenReturn(List.of());

            CalendarResponse response = calendarService.getCalendar(userId);

            assertThat(response.days()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(response.days())
                .allSatisfy(day -> assertThat(day.availabilities())
                    .allSatisfy(availability -> assertThat(availability.status())
                        .isEqualTo(SpotStatusEnum.FREE)));
        }
    }
}
