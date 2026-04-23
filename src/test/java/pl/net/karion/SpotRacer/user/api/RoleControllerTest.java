package pl.net.karion.SpotRacer.user.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;

import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

public class RoleControllerTest  extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserFixture userFixture;

    @Test
    void shouldAddRole() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "roleadd-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Renata", "Rolowa");

        String body = """
            {
                "roles": [
                    "ADMIN"
                ]
            }
        """;

        mockMvc.perform(post("/api/user/{id}/role", saved.getId().toString())
            .contentType(APPLICATION_JSON)
            .content(body)
            .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId().toString()))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.firstname").value("Renata"))
            .andExpect(jsonPath("$.lastname").value("Rolowa"))
            .andExpect(jsonPath("$.roles", hasSize(2)))
            .andExpect(jsonPath("$.roles", containsInAnyOrder("USER", "ADMIN")))
        ;
    }

    @Test
    void shouldNotAddSameRole() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "roleadd-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Renata", "Rolowa");

        String body = """
            {
                "roles": [
                    "USER"
                ]
            }
        """;

        mockMvc.perform(post("/api/user/{id}/role", saved.getId().toString())
                        .contentType(APPLICATION_JSON)
                        .content(body)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstname").value("Renata"))
                .andExpect(jsonPath("$.lastname").value("Rolowa"))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("USER")))
        ;
    }

    @Test
    void shouldFailAddingRoleBecauseOfAuthorization() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "roleadd-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Renata", "Rolowa");

        String body = """
            {
                "roles": [
                    "ADMIN"
                ]
            }
        """;

        mockMvc.perform(post("/api/user/{id}/role", saved.getId().toString())
                .contentType(APPLICATION_JSON)
                .content(body)
                .with(user("user").roles("USER")))
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldDeleteRoles() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "roleadd-api-" + suffix + "@example.com";

        User saved = userFixture.createAdmin(email);

        String body = """
            {
                "roles": [
                    "ADMIN"
                ]
            }
        """;

        mockMvc.perform(delete("/api/user/{id}/role", saved.getId().toString())
                        .contentType(APPLICATION_JSON)
                        .content(body)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstname").value("Ada"))
                .andExpect(jsonPath("$.lastname").value("Admin"))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("USER")))
        ;
    }

    @Test
    void shouldNotDeleteLastRoles() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "roleadd-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Renata", "Rolowa");

        String body = """
            {
                "roles": [
                    "USER"
                ]
            }
        """;

        mockMvc.perform(delete("/api/user/{id}/role", saved.getId().toString())
                        .contentType(APPLICATION_JSON)
                        .content(body)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
        ;
    }


    @Test
    void shouldFailDeletingRoleBecauseOfAuthorization() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "roleadd-api-" + suffix + "@example.com";

        User saved = userFixture.createUser(email, "Renata", "Rolowa");

        String body = """
            {
                "roles": [
                    "ADMIN"
                ]
            }
        """;

        mockMvc.perform(delete("/api/user/{id}/role", saved.getId().toString())
            .contentType(APPLICATION_JSON)
            .content(body)
            .with(user("user").roles("USER")))
            .andExpect(status().isForbidden())
        ;
    }
}
