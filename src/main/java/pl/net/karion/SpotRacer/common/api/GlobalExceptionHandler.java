package pl.net.karion.SpotRacer.common.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.assignment.exception.AssignmentNotFoundException;
import pl.net.karion.SpotRacer.assignment.exception.SpotAlreadyAssignedException;
import pl.net.karion.SpotRacer.spot.exception.LocationNotFoundException;
import pl.net.karion.SpotRacer.spot.exception.SpotNotFoundException;
import pl.net.karion.SpotRacer.user.exception.UserEmailTakenException;
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

    @ExceptionHandler(LocationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(LocationNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(UserMustHaveRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUserMustHaveRole(UserMustHaveRoleException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(UserEmailTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserEmailTaken(UserEmailTakenException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(SpotNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleSpotNotFound(SpotNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(SpotAlreadyAssignedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleSpotAlreadyAssigned(SpotAlreadyAssignedException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(AssignmentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleAssignmentNotFound(AssignmentNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}