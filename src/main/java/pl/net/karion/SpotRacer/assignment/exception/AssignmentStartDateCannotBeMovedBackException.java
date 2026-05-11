package pl.net.karion.SpotRacer.assignment.exception;

public class AssignmentStartDateCannotBeMovedBackException extends RuntimeException {

    public static final String ASSIGNMENT_START_DATE_CANNOT_BE_MOVED_BACK = "Assignment start date cannot be moved back.";

    public AssignmentStartDateCannotBeMovedBackException(String message) {
        super(message);
    }

    public AssignmentStartDateCannotBeMovedBackException() {
        super(ASSIGNMENT_START_DATE_CANNOT_BE_MOVED_BACK);
    }
}
