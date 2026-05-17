package pl.net.karion.SpotRacer.security.model;

import java.util.Set;
import java.util.UUID;
import pl.net.karion.SpotRacer.user.model.Role;


public record CurrentUser(
        UUID id,
        Set<Role> roles
) {

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}
