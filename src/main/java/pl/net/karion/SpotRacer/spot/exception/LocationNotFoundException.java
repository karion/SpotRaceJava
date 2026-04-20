package pl.net.karion.SpotRacer.spot.exception;

public class LocationNotFoundException extends RuntimeException {

    public static final String LOCATION_NOT_FOUND = "Location not found.";

    public LocationNotFoundException(String message) {
        super(message);
    }

    public LocationNotFoundException() {
        super(LOCATION_NOT_FOUND);
    }
}
