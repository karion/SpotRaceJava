package pl.net.karion.SpotRacer.assignment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.net.karion.SpotRacer.assignment.api.controller.AssignmentCreateRequest;
import pl.net.karion.SpotRacer.assignment.api.controller.AssignmentResponse;
import pl.net.karion.SpotRacer.assignment.api.controller.AssignmentUpdateRequest;
import pl.net.karion.SpotRacer.assignment.exception.AssignmentNotFoundException;
import pl.net.karion.SpotRacer.assignment.exception.AssignmentStartDateCannotBeMovedBackException;
import pl.net.karion.SpotRacer.assignment.exception.SpotAlreadyAssignedException;
import pl.net.karion.SpotRacer.assignment.model.Assignment;
import pl.net.karion.SpotRacer.assignment.model.AssignmentRepository;
import pl.net.karion.SpotRacer.spot.exception.SpotNotFoundException;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.spot.model.SpotRepository;
import pl.net.karion.SpotRacer.user.exception.UserNotFoundException;
import pl.net.karion.SpotRacer.user.model.User;
import pl.net.karion.SpotRacer.user.model.UserRepository;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceTest {

    @Mock
    AssignmentRepository assignmentRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    SpotRepository spotRepository;

    @InjectMocks
    AssignmentService assignmentService;

    @Test
    void shouldReturnAssignmentWhenExists() {
        // given
        UUID id = UUID.randomUUID();

        User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
        );

        Spot spot = new Spot(UUID.randomUUID(), "A1", null);

        Assignment assignment = new Assignment(
                id,
                user,
                spot,
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 1, 10),
                "note"
        );

        when(assignmentRepository.findById(id))
                .thenReturn(Optional.of(assignment));

        // when
        AssignmentResponse response = assignmentService.getById(id);

        // then
        assertEquals(id, response.id());
        assertEquals("Jan Kowalski", response.userName());
        assertEquals("A1", response.spotName());
    }

    @Test
    void shouldThrowWhenAssignmentNotFound() {
        // given
        UUID id = UUID.randomUUID();

        when(assignmentRepository.findById(id))
                .thenReturn(Optional.empty());

        // when + then
        assertThrows(
                AssignmentNotFoundException.class,
                () -> assignmentService.getById(id)
        );
    }

    @Test
    void shouldCreateAssignment() {
        //given
        LocalDate startDate = LocalDate.of(2030, 12, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 20);

        User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Janina",
                "Nowacka"
        );

        Spot spot = new Spot(
                UUID.randomUUID(),
                "A1",
                null
        );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(spotRepository.findById(spot.getId()))
                .thenReturn(Optional.of(spot));

        when(assignmentRepository.findOverlapping(
                any(),
                any(),
                any(),
                isNull()
        )).thenReturn(List.of());

        UUID assignmentId = UUID.randomUUID();

        Assignment assignment = new Assignment(
                assignmentId,
                user,
                spot,
                startDate,
                endDate,
                "just note"
        );

        when(assignmentRepository.save(any())).thenReturn(assignment);

        // when
        AssignmentCreateRequest request = new AssignmentCreateRequest(
                user.getId(),
                spot.getId(),
                startDate,
                endDate,
                "just note"
        );

        AssignmentResponse response = assignmentService.create(request);
        //then
        verify(assignmentRepository).save(any());

        assertEquals(spot.getId(), response.spotId());
        assertEquals("A1", response.spotName());
        assertEquals(user.getId(), response.userId());
        assertEquals("Janina Nowacka", response.userName());
        assertEquals(startDate, response.startDate());
        assertEquals(endDate, response.endDate());
        assertEquals("just note", response.note());

    }

    @Test
    void shouldThrowSpotNotFoundOnCreateWithWrongSpotId() {

        //given
        UUID spotId = UUID.randomUUID();

        User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Janina",
                "Nowacka"
        );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        LocalDate startDate = LocalDate.of(2030, 12, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 20);

        when(spotRepository.findById(spotId))
                .thenReturn(Optional.empty());

        AssignmentCreateRequest request = new AssignmentCreateRequest(
                user.getId(),
                spotId,
                startDate,
                endDate,
                null
        );

        // when + then
        assertThrows(
                SpotNotFoundException.class,
                () -> assignmentService.create(request)
        );
    }

    @Test
    void shouldThrowUserNotFoundOnCreateWithWrongUserId() {

        //given
        UUID userId = UUID.randomUUID();
        Spot spot = new Spot(UUID.randomUUID(), "A1", null);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        LocalDate startDate = LocalDate.of(2030, 12, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 20);

        AssignmentCreateRequest request = new AssignmentCreateRequest(
                userId,
                spot.getId(),
                startDate,
                endDate,
                null
        );

        // when + then
        assertThrows(
                UserNotFoundException.class,
                () -> assignmentService.create(request)
        );
    }

    @Test
    void shouldThrowSpotAlreadyAssignedOnConflict() {
        //given
        LocalDate startDate = LocalDate.of(2030, 12, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 20);

        User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Janina",
                "Nowacka"
        );

        Spot spot = new Spot(
                UUID.randomUUID(),
                "A1",
                null
        );

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(spotRepository.findById(spot.getId()))
                .thenReturn(Optional.of(spot));


        Assignment assignment = new Assignment(
                UUID.randomUUID(),
                user,
                spot,
                startDate,
                endDate,
                "just note"
        );

        when(assignmentRepository.findOverlapping(
                any(),
                any(),
                any(),
                isNull()
        )).thenReturn(List.of(assignment));

        // when
        AssignmentCreateRequest request = new AssignmentCreateRequest(
                user.getId(),
                spot.getId(),
                startDate,
                endDate,
                "just note"
        );

        // when + then
        assertThrows(
                SpotAlreadyAssignedException.class,
                () -> assignmentService.create(request)
        );
    }

    @Test
    void shouldUpdateAssignment() {
        // given
        UUID assignmentId = UUID.randomUUID();

        User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
        );

        Spot spot = new Spot(UUID.randomUUID(), "A1", null);

        Assignment assignment = new Assignment(
                assignmentId,
                user,
                spot,
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 1, 10),
                "note"
        );


        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        when(assignmentRepository.findOverlapping(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(List.of());

        LocalDate startDate = LocalDate.of(2030, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 1, 15);

        Assignment updatedAssignment = new Assignment(
                assignmentId,
                user,
                spot,
                startDate,
                endDate,
                "just note"
        );

        when(assignmentRepository.save(any())).thenReturn(updatedAssignment);

        // when
        AssignmentUpdateRequest request = new AssignmentUpdateRequest(
                startDate,
                endDate,
                "just note"
        );

        AssignmentResponse response = assignmentService.update(assignmentId, request);

        //then
        assertEquals(spot.getId(), response.spotId());
        assertEquals("A1", response.spotName());
        assertEquals(user.getId(), response.userId());
        assertEquals("Jan Kowalski", response.userName());
        assertEquals(startDate, response.startDate());
        assertEquals(endDate, response.endDate());
        assertEquals("just note", response.note());
    }

    @Test
    void shouldThrowSpotAlreadyAssignedOnConflictWhenUpdating() {
        // given
        UUID assignmentId = UUID.randomUUID();

        User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
        );

        Spot spot = new Spot(UUID.randomUUID(), "A1", null);

        Assignment assignment = new Assignment(
                assignmentId,
                user,
                spot,
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 1, 10),
                "note"
        );

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        when(assignmentRepository.findOverlapping(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(List.of(assignment));

        LocalDate startDate = LocalDate.of(2030, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 1, 15);

        // when + then
        AssignmentUpdateRequest request = new AssignmentUpdateRequest(
                startDate,
                endDate,
                "just note"
        );

        assertThrows(
                SpotAlreadyAssignedException.class,
                () -> assignmentService.update(assignmentId, request)
        );
    }

    @Test
    void shouldThrowAssignmentNotFoundOnUpdate() {
        // given
        UUID assignmentId = UUID.randomUUID();

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.empty());

        // when + then
        LocalDate startDate = LocalDate.of(2030, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 1, 15);

        AssignmentUpdateRequest request = new AssignmentUpdateRequest(
                startDate,
                endDate,
                "just note"
        );

        assertThrows(
                AssignmentNotFoundException.class,
                () -> assignmentService.update(assignmentId, request)
        );
    }

    @Test
    void shouldThrowAssignmentStartDateCannotBeMovedBackOnUpdate() {
        // given
        UUID assignmentId = UUID.randomUUID();

        User user = new User(
                UUID.randomUUID(),
                "random@email.com",
                "",
                "Jan",
                "Kowalski"
        );

        Spot spot = new Spot(UUID.randomUUID(), "A1", null);

        Assignment assignment = new Assignment(
                assignmentId,
                user,
                spot,
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 1, 10),
                "note"
        );

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 1, 15);

        // when + then
        AssignmentUpdateRequest request = new AssignmentUpdateRequest(
                startDate,
                endDate,
                "just note"
        );

        assertThrows(
                AssignmentStartDateCannotBeMovedBackException.class,
                () -> assignmentService.update(assignmentId, request)
        );
    }
}
