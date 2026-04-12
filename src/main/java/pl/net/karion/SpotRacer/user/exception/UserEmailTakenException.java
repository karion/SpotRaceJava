package pl.net.karion.SpotRacer.user.exception;

public class UserEmailTakenException extends RuntimeException {

    public static final String USER_EMAIL_ALREADY_TAKEN_MESSAGE = "User with this email already exists";

    public UserEmailTakenException(String message) {
        super(message);
    }

    public UserEmailTakenException() {
        super(USER_EMAIL_ALREADY_TAKEN_MESSAGE);
    }
}
