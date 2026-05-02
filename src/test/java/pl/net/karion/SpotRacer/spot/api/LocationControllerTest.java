package pl.net.karion.SpotRacer.spot.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import pl.net.karion.SpotRacer.spot.fixtures.LocationFixture;
import pl.net.karion.SpotRacer.spot.model.Location;
import pl.net.karion.SpotRacer.support.IntegrationTest;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocationControllerTest  extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocationFixture locationFixture;

    @Test
    void shouldCreateLocation() throws Exception {
        String body = this.createLocationBody("location1");

        mockMvc.perform(post("/api/location")
            .with(user("admin").roles("ADMIN"))
            .contentType(APPLICATION_JSON)
            .content(body))

            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("location1"))
            .andExpect(jsonPath("$.description").value("Long description"))
        ;
    }

    @Test
    void shouldFailCreateLocationIfNotAdmin() throws Exception {
        String body =  this.createLocationBody("location1");

        mockMvc.perform(post("/api/location")
            .with(user("user").roles("USER"))
            .contentType(APPLICATION_JSON)
            .content(body))

            .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldFailCreateLocationIfBadData() throws Exception {
        String body = """
                {
                    "description": "Long description"
                }
                """;

        mockMvc.perform(post("/api/location")
            .with(user("admin").roles("ADMIN"))
            .contentType(APPLICATION_JSON)
            .content(body))

            .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void shouldGetLocationByIdAsAdmin() throws Exception {
        Location saved = locationFixture.createLocation("Ta lokalizacja");

        mockMvc.perform(get("/api/location/{id}", saved.getId())
            .with(user("admin").roles("ADMIN")))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId().toString()))
            .andExpect(jsonPath("$.name").value("Ta lokalizacja"))
            .andExpect(jsonPath("$.description").value("Not so random description"))
        ;
    }

    @Test
    void shouldReturnForbiddenWhenUserIsNotAdmin() throws Exception {
        Location saved = locationFixture.createLocation("Ta lokalizacja");

        mockMvc.perform(get("/api/location/{id}", saved.getId())
            .with(user("user").roles("USER")))

            .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldFailGetLocationByRandomIdAsAdmin() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/location/{id}", randomId)
            .with(user("admin").roles("ADMIN")))

            .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldUpdateLocation() throws Exception {
        Location saved = locationFixture.createLocation("Ta lokalizacja");

        String body =  this.createLocationBody("location1");

        mockMvc.perform(put("/api/location/{id}", saved.getId())
            .with(user("admin").roles("ADMIN"))
            .contentType(APPLICATION_JSON)
            .content(body))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("location1"))
            .andExpect(jsonPath("$.description").value("Long description"))
        ;
    }

    @Test
    void shouldFailUpdateLocationByUser() throws Exception {
        Location saved = locationFixture.createLocation("Ta lokalizacja");

        String body =  this.createLocationBody("location1");

        mockMvc.perform(put("/api/location/{id}", saved.getId())
            .with(user("user").roles("USER"))
            .contentType(APPLICATION_JSON)
            .content(body))

            .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldFailUpdateLocationByBadRequest() throws Exception {

        Location saved = locationFixture.createLocation("Ta lokalizacja");

        String body = """
                {
                    "description": "Long description"
                }
                """;

        mockMvc.perform(put("/api/location/{id}", saved.getId())
            .with(user("admin").roles("ADMIN"))
            .contentType(APPLICATION_JSON)
            .content(body))

            .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void shouldFailUpdateLocationByRandomId() throws Exception {
        UUID randomId = UUID.randomUUID();

        String body = this.createLocationBody("not found");

        mockMvc.perform(put("/api/location/{id}", randomId)
            .with(user("admin").roles("ADMIN"))
            .contentType(APPLICATION_JSON)
            .content(body))

            .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldGetLocationsList() throws Exception {
        locationFixture.createLocation("A");
        locationFixture.createLocation("B");

        mockMvc.perform(get("/api/location")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(2)))
        ;
    }

    @Test
    void shouldSearchOnLocationsList() throws Exception {
        locationFixture.createLocation("Tajna lokalizacja");

        mockMvc.perform(get("/api/location?search=tajna")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(1)))
        ;
    }

    private String createLocationBody(String name) {
        return """
        {
            "name": "%s",
            "description": "Long description"
        }
    """.formatted(name);
    }
}
