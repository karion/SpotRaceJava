package pl.net.karion.SpotRacer.user.api.controller;

import pl.net.karion.SpotRacer.user.model.Role;

import java.util.Set;
import java.util.UUID;

public class UserResponse{

    private final UUID id;
    private final String email;
    private final String firstname;
    private final String lastname;

    private final Set<Role> roles;

    public UserResponse(UUID id, String email, String firstname, String lastname, Set<Role> roles) {
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

    public Set<Role> getRoles() {
        return roles;
    }
}