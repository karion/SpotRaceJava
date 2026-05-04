package pl.net.karion.SpotRacer.assignment.service;

import pl.net.karion.SpotRacer.assignment.api.controller.AssignmentResponse;
import pl.net.karion.SpotRacer.assignment.model.Assignment;

public class AssignmentMapper {

    public static AssignmentResponse toResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getSpot().getId(),
                assignment.getSpot().getName(),
                assignment.getUser().getId(),
                assignment.getUser().getFirstname() + " " +assignment.getUser().getLastname(),
                assignment.getStartDate(),
                assignment.getEndDate(),
                assignment.getNote()
        );
    }
}
