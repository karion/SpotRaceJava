package pl.net.karion.SpotRacer.user.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.karion.SpotRacer.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositorySmokeTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldLoadContextAndRepository() {
        assertThat(userRepository).isNotNull();
    }
}