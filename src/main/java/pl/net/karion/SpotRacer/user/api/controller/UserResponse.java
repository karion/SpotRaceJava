package pl.net.karion.SpotRacer.user.api.controller;

import pl.net.karion.SpotRacer.user.model.Role;
import pl.net.karion.SpotRacer.user.model.User;

import java.util.List;
import java.util.UUID;

public class UserResponse{

    private final UUID id;
    private final String email;
    private final String firstname;
    private final String lastname;

    private final List<Role> roles;

    public UserResponse(UUID id, String email, String firstname, String lastname, List<Role> roles) {
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.roles = roles;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public List<Role> getRoles() {
        return roles;
    }
}