package pl.net.karion.SpotRacer.user.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;
import java.util.UUID;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;


class UserControllerTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserFixture userFixture;

    @Test
    void shouldCreateUser() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "john-api-" + suffix + "@example.com";
        String body = """
                {
                  "email": "%s",
                  "password": "secret123",
                  "firstname": "John",
                  "lastname": "Doe"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/user")
                .contentType(APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
        ;
    }

    @Test
    void shouldGetUserByIdAsAdmin() throws Exception {

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "pp-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Paulina", "Pobrana");

        mockMvc.perform(get("/api/user/{id}", saved.getId().toString())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstname").value("Paulina"))
                .andExpect(jsonPath("$.lastname").value("Pobrana"))
        ;
    }

    @Test
    void shouldNotGetUserByRandomUuid() throws Exception {

        UUID id =  UUID.randomUUID();

        mockMvc.perform(get("/api/user/{id}", id.toString())
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldFailAuthorisation() throws Exception {
        UUID id =  UUID.randomUUID();

        mockMvc.perform(get("/api/user/{id}", id.toString())
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldFailWhenCreateWithSameEmail() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "pp-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Paulina", "Pobrana");

        String body = """
                {
                  "email": "%s",
                  "password": "secret123",
                  "firstname": "John",
                  "lastname": "Doe"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/user")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
        ;
    }

    @Test
    void shouldFailOnIncorectData() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "pp-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Paulina", "Pobrana");

        String body = """
                {
                  "email": "%s",
                  "firstname": "John",
                  "lastname": "Doe"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/user")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void shouldFindUserWithSearch() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "pp-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Sara", "Szukana");


        mockMvc.perform(get("/api/user?search=szukana")
                        .contentType(APPLICATION_JSON)
                        .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.numberOfElements", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
        ;
    }
}