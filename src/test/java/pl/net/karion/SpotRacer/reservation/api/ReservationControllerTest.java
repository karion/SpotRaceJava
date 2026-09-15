package pl.net.karion.SpotRacer.reservation.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import pl.net.karion.SpotRacer.spot.fixtures.SpotFixture;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.net.karion.SpotRacer.reservation.exception.ReservationRequiresSelfOrAdminException.RESERVATION_REQUIRES_SELF_OR_ADMIN_MESSAGE;

class ReservationControllerTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private SpotFixture spotFixture;

    @Autowired
    private Clock clock;

    @Test
    void shouldCreateReservationForCurrentUser() throws Exception {
        // Given: użytkownik w bazie, wolne miejsce, poprawna data.
        Spot spot = this.spotFixture.createSpot("Spot for reservation");
        User user = this.userFixture.createUser(
            UserFixture.randomEmail(),
            "Renata",
            "Rezewująca"
        );

        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);

        String body = this.createBody(user.getId(), spot.getId(), tomorrow.toString());

        // When: POST /api/reservation z JWT i danymi rezerwacji.

        mockMvc.perform(post("/api/reservation")
                .with(jwt()
                    .jwt(builder -> builder.subject(user.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )

            // Then: status 201, poprawne userId, spotId i date.
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.spotId").value(spot.getId().toString()))
            .andExpect(jsonPath("$.userId").value(user.getId().toString()))
            .andExpect(jsonPath("$.date").value(tomorrow.toString()))
        ;
    }

    @Test
    void shouldForbidReservationForAnotherUser() throws Exception {
        // Given: dwóch użytkowników, wolne miejsce, poprawna data.
        Spot spot = this.spotFixture.createSpot("Spot for reservation");
        User user = this.userFixture.createUser(
            UserFixture.randomEmail(),
            "Renata",
            "Rezewująca"
        );

        User otherUser = this.userFixture.createUser(
            UserFixture.randomEmail(),
            "Tina",
            "Tajniak"
        );

        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);

        String body = this.createBody(user.getId(), spot.getId(), tomorrow.toString());

        // When: POST /api/reservation

        mockMvc.perform(post("/api/reservation")
        // JWT należy do pierwszego, request wskazuje drugiego.
                .with(jwt()
                    .jwt(builder -> builder.subject(otherUser.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )

            // Then: status 403, rezerwacja nie została zapisana.
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value(RESERVATION_REQUIRES_SELF_OR_ADMIN_MESSAGE))
        ;
    }

    @Test
    void shouldAllowAdminToReserveForAnotherUser() throws Exception {
        // Given: administrator, inny użytkownik i wolne miejsce.
        Spot spot = this.spotFixture.createSpot("Spot for reservation");
        User user = this.userFixture.createUser(
            UserFixture.randomEmail(),
            "Renata",
            "Rezewująca"
        );

        User adminUser = this.userFixture.createUser(
            UserFixture.randomEmail(),
            "Ada",
            "Adminka"
        );

        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);

        String body = this.createBody(user.getId(), spot.getId(), tomorrow.toString());

        // When: POST /api/reservation dla drugiego użytkownika.
        mockMvc.perform(post("/api/reservation")
                // JWT administratora z uprawnieniem ROLE_ADMIN.
                .with(jwt()
                    .jwt(builder -> builder.subject(adminUser.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )

            // Then: status 201, userId wskazuje odbiorcę rezerwacji.
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.spotId").value(spot.getId().toString()))
            .andExpect(jsonPath("$.userId").value(user.getId().toString()))
            .andExpect(jsonPath("$.date").value(tomorrow.toString()))
        ;
    }

    @Test
    void shouldRejectReservationWithoutAuthentication() throws Exception {
        Spot spot = this.spotFixture.createSpot("Spot for reservation");
        User user = this.userFixture.createUser(
            UserFixture.randomEmail(),
            "Renata",
            "Rezewująca"
        );

        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);

        String body = this.createBody(user.getId(), spot.getId(), tomorrow.toString());

        // When: POST /api/reservation bez tokenu.

        mockMvc.perform(post("/api/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )

            // Then: status 401.
            .andExpect(status().isUnauthorized())
        ;
    }

    private String createBody(UUID userId, UUID spotId, String date) {
        String body = """
            {
              "userId": "%s",
              "spotId": "%s",
              "date": "%s"
            }
            """;

        return body.formatted(
            userId.toString(),
            spotId.toString(),
            date
        );
    }
}
