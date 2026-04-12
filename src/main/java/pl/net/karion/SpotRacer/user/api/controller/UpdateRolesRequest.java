package pl.net.karion.SpotRacer.user.api.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pl.net.karion.SpotRacer.user.model.Role;

import java.util.Set;

public record UpdateRolesRequest(
        @NotEmpty
        Set<@NotNull Role> roles
) {
}
