package pl.net.karion.SpotRacer.assignment.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final SpotRepository spotRepository;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            SpotRepository spotRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.spotRepository = spotRepository;
    }

    @Transactional
    public AssignmentResponse create(AssignmentCreateRequest request) {
        User user = this.getUser(request.userId());
        Spot spot = this.getSpot(request.spotId());

        //checks
        if (!this.canAssignSpot(spot.getId(), request.startDate(), request.endDate(), null)) {
            throw new SpotAlreadyAssignedException();
        }

        Assignment assignment = new Assignment(
            UUID.randomUUID(),
            user,
            spot,
            request.startDate(),
            request.endDate(),
            request.note()
        );

        Assignment saved = this.assignmentRepository.save(assignment);

        return AssignmentMapper.toResponse(saved);
    }

    private boolean canAssignSpot(UUID spotId, LocalDate startDate, LocalDate endDate, UUID assignmentId) {
        List<Assignment> overlapped = this.assignmentRepository.findOverlapping(
                spotId,
                startDate,
                endDate,
                assignmentId
        );

        return overlapped.isEmpty();
    }

    private User getUser(UUID id) {
        return this.userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    private Spot getSpot(UUID id) {
        return this.spotRepository.findById(id).orElseThrow(SpotNotFoundException::new);
    }

    public AssignmentResponse getById(UUID id) {
        return AssignmentMapper.toResponse(
            this.assignmentRepository.findById(id)
                .orElseThrow(AssignmentNotFoundException::new)
        );
    }

    @Transactional
    public AssignmentResponse update(UUID id, @Valid AssignmentUpdateRequest request) {
        Assignment assignment = this.assignmentRepository.findById(id)
                .orElseThrow(AssignmentNotFoundException::new);

        if (request.startDate().isBefore(assignment.getStartDate())) {
            throw new AssignmentStartDateCannotBeMovedBackException();
        }

        if (!this.canAssignSpot(
                assignment.getSpot().getId(),
                request.startDate(),
                request.endDate(),
                assignment.getId())
        ) {
            throw new SpotAlreadyAssignedException();
        }

        assignment.setStartDate(request.startDate());
        assignment.setEndDate(request.endDate());
        assignment.setNote(request.note());

        return AssignmentMapper.toResponse(
            this.assignmentRepository.save(assignment)
        );
    }

    @Transactional
    public void delete(UUID id) {
        Assignment assignment = this.assignmentRepository.findById(id)
                .orElseThrow(AssignmentNotFoundException::new);

        this.assignmentRepository.delete(assignment);
    }


}
