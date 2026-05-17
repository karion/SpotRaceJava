package pl.net.karion.SpotRacer.reservation.exception;

public class ReservationAlreadyTakenException extends RuntimeException {
    public static final String RESERVATION_ALREADY_TAKEN = "Reservation already taken";

    public ReservationAlreadyTakenException(String message) {
        super(message);
    }

    public ReservationAlreadyTakenException() {
        super(RESERVATION_ALREADY_TAKEN);
    }
}
