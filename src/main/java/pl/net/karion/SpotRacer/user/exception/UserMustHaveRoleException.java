package pl.net.karion.SpotRacer.user.exception;

public class UserMustHaveRoleException extends RuntimeException {

    public static final String USER_MUST_HAVE_ROLES_MESSAGE = "User must have at least one Role.";

    public UserMustHaveRoleException(String message) {
        super(message);
    }
    
    public UserMustHaveRoleException() {
        super(USER_MUST_HAVE_ROLES_MESSAGE);
    }
}
