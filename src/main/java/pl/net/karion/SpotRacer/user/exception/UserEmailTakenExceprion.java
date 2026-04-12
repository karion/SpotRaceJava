package pl.net.karion.SpotRacer.user.exception;

public class UserEmailTakenExceprion extends RuntimeException {

    public static final String USER_EMAIL_ALREADY_TAKEN_MESSAGE = "User with this email already exists";

    public UserEmailTakenExceprion(String message) {
        super(message);
    }

    public UserEmailTakenExceprion() {
        super(USER_EMAIL_ALREADY_TAKEN_MESSAGE);
    }
}
