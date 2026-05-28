package pl.net.karion.SpotRacer.reservation.exception;

public class ReservationToFarInFutureException extends RuntimeException {
    public static final String RESERVATION_TO_FAR_IN_FUTURE = "Reservation to far in future";

    public ReservationToFarInFutureException(String message) {
        super(message);
    }

    public ReservationToFarInFutureException() {
        super(RESERVATION_TO_FAR_IN_FUTURE);
    }
}
