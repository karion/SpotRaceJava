package pl.net.karion.SpotRacer.assignment.exception;

public class SpotAlreadyAssignedException extends RuntimeException {

    public static final String SPOT_ALREADY_ASSIGNED = "Spot is already assigned";

    public SpotAlreadyAssignedException(String message) {
        super(message);
    }

    public SpotAlreadyAssignedException() {
        super(SPOT_ALREADY_ASSIGNED);
    }
}
