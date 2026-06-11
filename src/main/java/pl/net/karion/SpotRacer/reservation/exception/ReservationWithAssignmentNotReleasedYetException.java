package pl.net.karion.SpotRacer.reservation.exception;

public class ReservationWithAssignmentNotReleasedYetException extends RuntimeException {
    public static final String RESERVATION_WITH_ASSIGNMENT_NOT_RELEASED_YET = "Reservation with Assignment not released yet";

    public ReservationWithAssignmentNotReleasedYetException(String message) {
        super(message);
    }

    public ReservationWithAssignmentNotReleasedYetException() {
        super(RESERVATION_WITH_ASSIGNMENT_NOT_RELEASED_YET);
    }
}
