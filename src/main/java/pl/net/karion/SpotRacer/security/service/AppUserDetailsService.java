package pl.net.karion.SpotRacer.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.net.karion.SpotRacer.user.model.User;
import pl.net.karion.SpotRacer.user.model.UserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByEmail(username)
                .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(
                        user.getRoles().stream()
                                .map(role -> "ROLE_" + role.name())
                                .toArray(String[]::new)
                )
                .build();
    }
}
