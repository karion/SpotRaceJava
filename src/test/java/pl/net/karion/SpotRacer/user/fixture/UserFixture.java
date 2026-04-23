package pl.net.karion.SpotRacer.user.fixture;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.net.karion.SpotRacer.user.model.Role;
import pl.net.karion.SpotRacer.user.model.User;
import pl.net.karion.SpotRacer.user.model.UserRepository;

import java.util.UUID;

@Component
public class UserFixture {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserFixture(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser() {
        return createUser("tine.test@example.com", "Tina", "Testówna");
    }

    public User createUser(String email, String firstname, String lastname) {
        User user = new User(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode("password123"),
                firstname,
                lastname
        );
        user.addRole(Role.USER);
        return userRepository.save(user);
    }

    public User createAdmin(String email) {
        User user = new User(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode("password123"),
                "Ada",
                "Admin"
        );
        user.addRole(Role.USER);
        user.addRole(Role.ADMIN);
        return userRepository.save(user);
    }
}