package pl.net.karion.SpotRacer.spot.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import pl.net.karion.SpotRacer.spot.fixtures.LocationFixture;
import pl.net.karion.SpotRacer.spot.fixtures.SpotFixture;
import pl.net.karion.SpotRacer.spot.model.Location;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.support.IntegrationTest;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SpotControllerTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocationFixture locationFixture;

    @Autowired
    private SpotFixture spotFixture;

    @Test
    void shouldCreateSpot() throws Exception {
        Location location = this.locationFixture.createLocation();

        String body = this.createLocationBody("Created spot", location);

        mockMvc.perform(post("/api/spot")
                .with(user("admin").roles("ADMIN"))
                .contentType(APPLICATION_JSON)
                .content(body))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Created spot"))
                .andExpect(jsonPath("$.locationId").value(location.getId().toString()))
                .andExpect(jsonPath("$.locationName").value(location.getName()))
        ;
    }

    @Test
    void shouldCreateSpotWithoutLocation() throws Exception {
        String body = this.createLocationBody("Created spot", null);

        mockMvc.perform(post("/api/spot")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Created spot"))
                .andExpect(jsonPath("$.locationId").isEmpty())
                .andExpect(jsonPath("$.locationName").isEmpty())
        ;
    }

    @Test
    void shouldReturnForbiddenWhenUserIsNotAdmin() throws Exception {
        String body = this.createLocationBody("Created spot", null);

        mockMvc.perform(post("/api/spot")
                        .with(user("user").roles("USER"))
                        .contentType(APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldReturnBadRequestIfIncompleteData() throws Exception {
        String body = """
                {
                    "nameXX": "Not name"
                }
            """;

        mockMvc.perform(post("/api/spot")
            .with(user("admin").roles("ADMIN"))
            .contentType(APPLICATION_JSON)
            .content(body))

            .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void shouldReturnNotFoundIfIncorrectLocation() throws Exception {
        String body = """
                {
                    "name": "Incorrect location",
                    "locationId": "%s"
                }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/spot")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldReturnSpotByIdByAdmin() throws Exception {

        Spot created = this.spotFixture.createSpot("GET spot", "Spotted location");

        mockMvc.perform(get("/api/spot/{id}", created.getId())
                        .with(user("admin").roles("ADMIN"))
                        )

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("GET spot"))
                .andExpect(jsonPath("$.locationId").value(created.getLocation().getId().toString()))
                .andExpect(jsonPath("$.locationName").value("Spotted location"))
        ;
    }

    @Test
    void shouldReturnForbiddenByUser() throws Exception {

        Spot created = this.spotFixture.createSpot("GET spot", "Spotted location");

        mockMvc.perform(get("/api/spot/{id}", created.getId())
                        .with(user("user").roles("USER"))
                )

                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldReturnNotFoundOnRandomUuid() throws Exception {
        mockMvc.perform(get("/api/spot/{id}", UUID.randomUUID())
                        .with(user("admin").roles("ADMIN"))
                )

                .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldUpdateSpotByAdmin() throws Exception {
        Spot created = this.spotFixture.createSpot("Created spot for update", "Spotted location");

        Location location = this.locationFixture.createLocation("New location");
        String body = this.createLocationBody("Updated spot", location);

        mockMvc.perform(put("/api/spot/{id}", created.getId())
                    .with(user("admin").roles("ADMIN"))
                    .contentType(APPLICATION_JSON)
                    .content(body)
                )

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated spot"))
                .andExpect(jsonPath("$.locationId").value(location.getId().toString()))
                .andExpect(jsonPath("$.locationName").value("New location"))
        ;
    }

    @Test
    void shouldReturnForbiddenOnUpdateSpotByUserAdmin() throws Exception {
        Spot created = this.spotFixture.createSpot("Created spot for update", "Spotted location");

        Location location = this.locationFixture.createLocation("New location");
        String body = this.createLocationBody("Updated spot", location);

        mockMvc.perform(put("/api/spot/{id}", created.getId())
                    .with(user("user").roles("USER"))
                    .contentType(APPLICATION_JSON)
                    .content(body)
                )

                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldReturnNotFoundOnUpdateOnRandomSpotId() throws Exception {

        Location location = this.locationFixture.createLocation("New location");
        String body = this.createLocationBody("Updated spot", location);

        mockMvc.perform(put("/api/spot/{id}", UUID.randomUUID())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(APPLICATION_JSON)
                        .content(body)
                )

                .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldReturnNotFoundOnUpdateOnRandomLocation() throws Exception {
        Spot created = this.spotFixture.createSpot("Created spot for update", "Spotted location");

        String body = """
                {
                    "name": "Incorrect location",
                    "locationId": "%s"
                }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(put("/api/spot/{id}", created.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(APPLICATION_JSON)
                        .content(body)
                )

                .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldReturnListOfSpotByAdmin() throws Exception {

        this.spotFixture.createSpot("GET spot1", "Spotted location");
        this.spotFixture.createSpot("GET spot2", "Spotted location");

        mockMvc.perform(get("/api/spot")
                        .with(user("admin").roles("ADMIN"))
                )

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(2)))
        ;
    }

    @Test
    void shouldReturnForbiddenOnListOfSpotByUser() throws Exception {

        this.spotFixture.createSpot("GET spot1", "Spotted location");
        this.spotFixture.createSpot("GET spot2", "Spotted location");

        mockMvc.perform(get("/api/spot")
                        .with(user("user").roles("USER"))
                )

                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldSearchListOfSpotByAdmin() throws Exception {

        this.spotFixture.createSpot("Unique spot", "Spotted location");

        mockMvc.perform(get("/api/spot?search=unique")
                        .with(user("admin").roles("ADMIN"))
                )

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].name").value(hasItem("Unique spot")))
        ;
    }

    @Test
    void shouldSearchListOfSpotByLocationNameByAdmin() throws Exception {

        this.spotFixture.createSpot("Next spot", "a mysterious place");

        mockMvc.perform(get("/api/spot?search=mysterious")
                        .with(user("admin").roles("ADMIN"))
                )

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[*].locationName").value(hasItem("a mysterious place")))
        ;
    }


    private String createLocationBody(String name, Location location) {

        if (location == null) {
            return """
                {
                    "name": "%s"
                }
            """.formatted(name);
        }

        return """
            {
                "name": "%s",
                "locationId": "%s"
            }
        """.formatted(name, location.getId());
    }
}
