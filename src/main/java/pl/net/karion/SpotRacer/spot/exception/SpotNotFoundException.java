package pl.net.karion.SpotRacer.spot.exception;

public class SpotNotFoundException extends RuntimeException {

    public static final String SPOT_NOT_FOUND = "Spot not found.";

    public SpotNotFoundException(String message) {
        super(message);
    }

    public SpotNotFoundException() {
        super(SPOT_NOT_FOUND);
    }
}
