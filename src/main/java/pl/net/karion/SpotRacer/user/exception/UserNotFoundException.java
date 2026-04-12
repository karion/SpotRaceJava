package pl.net.karion.SpotRacer.user.exception;

public class UserNotFoundException extends RuntimeException {

    public static final String USER_NOT_FOUND_MESSAGE = "User not found.";

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException() {
        super(USER_NOT_FOUND_MESSAGE);
    }
}
