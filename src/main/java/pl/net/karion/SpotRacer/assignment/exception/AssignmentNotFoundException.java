package pl.net.karion.SpotRacer.assignment.exception;

public class AssignmentNotFoundException extends RuntimeException {

    public static final String ASSIGNMENT_NOT_FOUND = "Assignment not found.";

    public AssignmentNotFoundException(String message) {
        super(message);
    }

    public AssignmentNotFoundException() {
        super(ASSIGNMENT_NOT_FOUND);
    }
}
