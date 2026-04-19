package pl.net.karion.SpotRacer.security.model;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.net.karion.SpotRacer.user.model.Role;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {
    private final UUID id;
    private String email;
    private String passwordHash;
    private List<Role> roles;

    public UserDetails(
            UUID id, String email, String passwordHash, List<Role> roles
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public List<Role> getRoles() {
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public @Nullable String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public UUID getId() {
        return id;
    }
}
