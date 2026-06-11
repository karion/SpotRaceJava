package pl.net.karion.SpotRacer.reservation.exception;

public class ReservationRequiresSelfOrAdminException extends RuntimeException  {
    public static final String RESERVATION_REQUIRES_SELF_OR_ADMIN_MESSAGE = "Reservation requires self or admin";

    public ReservationRequiresSelfOrAdminException(String message) {
        super(message);
    }

    public ReservationRequiresSelfOrAdminException() {
        super(RESERVATION_REQUIRES_SELF_OR_ADMIN_MESSAGE);
    }
}
