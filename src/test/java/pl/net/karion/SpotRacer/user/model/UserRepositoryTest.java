package pl.net.karion.SpotRacer.user.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFixture userFixture;

    @Test
    void shouldFindUserByEmail() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "john" + suffix + "@example.com";
        User saved = userFixture.createUser(email, "John", "Doe");

        Optional<User> result = userRepository.findByEmail(email);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "john" + suffix + "@example.com";
        userFixture.createUser(email, "John", "Doe");

        boolean exists = userRepository.existsByEmail(email);

        assertThat(exists).isTrue();
    }
}