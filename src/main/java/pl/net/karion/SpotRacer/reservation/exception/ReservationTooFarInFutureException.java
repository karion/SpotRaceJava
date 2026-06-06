package pl.net.karion.SpotRacer.reservation.exception;

public class ReservationTooFarInFutureException extends RuntimeException {
    public static final String RESERVATION_TOO_FAR_IN_FUTURE = "Reservation too far in future";

    public ReservationTooFarInFutureException(String message) {
        super(message);
    }

    public ReservationTooFarInFutureException() {
        super(RESERVATION_TOO_FAR_IN_FUTURE);
    }
}
