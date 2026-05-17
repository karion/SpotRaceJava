package pl.net.karion.SpotRacer.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.net.karion.SpotRacer.security.model.CurrentUser;
import pl.net.karion.SpotRacer.security.model.UserDetails;

import java.util.Set;

@Component
class SpringSecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser currentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        UserDetails principal =
                (UserDetails) authentication.getPrincipal();

        return new CurrentUser(
                principal.getId(),
                Set.copyOf(principal.getRoles())
        );
    }
}
