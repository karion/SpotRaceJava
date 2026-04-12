package pl.net.karion.SpotRacer.common.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.user.exception.UserEmailTakenExceprion;
import pl.net.karion.SpotRacer.user.exception.UserMustHaveRoleException;
import pl.net.karion.SpotRacer.user.exception.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(UserMustHaveRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUserMustHaveRole(UserMustHaveRoleException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(UserEmailTakenExceprion.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserEmailTaken(UserEmailTakenExceprion ex) {
        return new ErrorResponse(ex.getMessage());
    }
}