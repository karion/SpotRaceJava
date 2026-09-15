package pl.net.karion.SpotRacer.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import pl.net.karion.SpotRacer.security.model.CurrentUser;
import pl.net.karion.SpotRacer.user.model.Role;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser currentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();

        Set<Role> roles =
            authentication.getAuthorities()
                .stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .filter(s ->s.startsWith("ROLE_"))
                .map(s -> Role.valueOf(s.substring("ROLE_".length())))
                .collect(Collectors.toSet())
        ;

        return new CurrentUser(
                UUID.fromString(jwt.getSubject()),
                roles
        );
    }
}
