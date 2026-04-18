package pl.net.karion.SpotRacer.security.service;

import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.net.karion.SpotRacer.security.model.UserDetails;
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
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByEmail(username)
                .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));

        return new UserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                List.copyOf(user.getRoles())
        );
    }
}
