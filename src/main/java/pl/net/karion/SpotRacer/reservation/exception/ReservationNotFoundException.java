package pl.net.karion.SpotRacer.reservation.exception;

public class ReservationNotFoundException extends RuntimeException {
    public static final String RESERVATION_NOT_FOUND = "Reservation not found";

    public ReservationNotFoundException(String message) {
        super(message);
    }

    public ReservationNotFoundException() {
        super(RESERVATION_NOT_FOUND);
    }
}
