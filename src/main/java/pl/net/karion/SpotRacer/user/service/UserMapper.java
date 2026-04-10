package pl.net.karion.SpotRacer.user.service;


import pl.net.karion.SpotRacer.user.api.controller.UserResponse;
import pl.net.karion.SpotRacer.user.model.User;

public class UserMapper {
    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstname(),
                user.getLastname(),
                user.getRoles()
        );
    }
}
