package pl.net.karion.SpotRacer.security.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.net.karion.SpotRacer.reservation.fixtures.ReservationFixture;
import pl.net.karion.SpotRacer.reservation.model.Reservation;
import pl.net.karion.SpotRacer.spot.fixtures.SpotFixture;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.*;

class LoginControllerTest extends IntegrationTest {

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private SpotFixture spotFixture;

    @Autowired
    private ReservationFixture reservationFixture;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Clock clock;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void shouldHaveSameExpirationTimeInTokenAndInResponse() throws Exception {
        User user = userFixture.createUser();

        String body = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(user.getEmail(), UserFixture.UNIVERSAL_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isOk())
            .andReturn()
        ;

        String json = result.getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.token");
        String expiresAt = JsonPath.read(json, "$.expiresAt");

        Instant expiresAtInstant = Instant.parse(expiresAt);

        Instant tokenExpiry = jwtDecoder.decode(token).getExpiresAt();

        assertThat(expiresAtInstant).isEqualTo(tokenExpiry);
    }

    @Test
    void shouldAuthoriseWithTokenFromLogin() throws Exception {
        User user = userFixture.createUser();

        String body = """
            {
                "email": "%s",
                "password": "%s"
            }
        """.formatted(user.getEmail(), UserFixture.UNIVERSAL_PASSWORD);

        MvcResult result = mockMvc
            .perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isOk())
            .andReturn()
        ;

        String json = result.getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.token");

        Spot spot = spotFixture.createSpot();
        Reservation reservation = reservationFixture.createReservation(
            user,
            spot,
            LocalDate.now(clock).plusDays(1)
        );

        mockMvc.perform(get("/api/me/reservations/current")
                .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()", equalTo(1)))
            .andExpect(jsonPath("$.content[0].userId", equalTo(user.getId().toString())))
            .andExpect(jsonPath("$.content[0].id", equalTo(reservation.getId().toString())))
        ;
    }

    @Test
    void shouldForbidAdminOnlyTokenOnUserEndpoint() throws Exception {
        User user = userFixture.createAdminOnlyRole(userFixture.randomEmail());

        String body = """
            {
                "email": "%s",
                "password": "%s"
            }
        """.formatted(user.getEmail(), UserFixture.UNIVERSAL_PASSWORD);

        MvcResult result = mockMvc
            .perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isOk())
            .andReturn()
        ;

        String json = result.getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.token");

        Spot spot = spotFixture.createSpot();
        Reservation reservation = reservationFixture.createReservation(
            user,
            spot,
            LocalDate.now(clock).plusDays(1)
        );

        mockMvc.perform(get("/api/me/reservations/current")
                .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isForbidden())
        ;
    }
}