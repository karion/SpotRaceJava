package pl.net.karion.SpotRacer.reservation.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.net.karion.SpotRacer.reservation.exception.ReservationNotFoundException;
import pl.net.karion.SpotRacer.reservation.fixtures.ReservationFixture;
import pl.net.karion.SpotRacer.reservation.model.Reservation;
import pl.net.karion.SpotRacer.reservation.model.ReservationRepository;
import pl.net.karion.SpotRacer.spot.fixtures.SpotFixture;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.Role;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.*;


class MyReservationControllerTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private SpotFixture spotFixture;

    @Autowired
    private ReservationFixture reservationFixture;

    @MockitoBean
    private Clock clock;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.of("Europe/Warsaw");
        LocalDate today = LocalDate.now(zone);
        Instant beforeRelease = today.atTime(6, 59).atZone(zone).toInstant();

        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(beforeRelease);
    }

    @Test
    void shouldReturnMyReservations() throws Exception {

        LocalDate today = LocalDate.now(clock);

        User user = this.userFixture.createUser();
        User otherUser = this.userFixture.createUser();

        Spot spot = this.spotFixture.createSpot();

        Reservation reservation1 =  this.reservationFixture.createReservation(
            user,
            spot,
            today
        );

        Reservation reservation2 =  this.reservationFixture.createReservation(
            user,
            spot,
            today.plus(1, ChronoUnit.DAYS)
        );

        Reservation otherUserReservation =  this.reservationFixture.createReservation(
            otherUser,
            spot,
            today.plus(2, ChronoUnit.DAYS)
        );

        mockMvc.perform(get("/api/me/reservations/current")
                .with(jwt()
                    .jwt(builder -> builder.subject(user.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()", equalTo(2)))
            .andExpect(jsonPath("$.content[*].userId", everyItem(is(user.getId().toString()))))
            .andExpect(jsonPath("$.content[*].id",
                containsInAnyOrder(
                    reservation1.getId().toString(),
                    reservation2.getId().toString()
                )
            ))
        ;
    }

    @Test
    void shouldReturnReservationsInOrder() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);

        User user = this.userFixture.createUser();
        Spot spotAA = this.spotFixture.createSpot("A spot", "Andora");
        Spot spotAB = this.spotFixture.createSpot("B spot", "Andora");
        Spot spotBA = this.spotFixture.createSpot("A spot", "Belgia");
        Spot spotBB = this.spotFixture.createSpot("B spot", "Belgia");
        Spot spot0C = this.spotFixture.createSpotWithoutLocation("C spot");

        Reservation reservation1 =  this.reservationFixture.createReservation(user, spotAA, today);
        Reservation reservation2 =  this.reservationFixture.createReservation(user, spotAB, today);
        Reservation reservation3 =  this.reservationFixture.createReservation(user, spotBA, today);
        Reservation reservation4 =  this.reservationFixture.createReservation(user, spotBB, today);
        Reservation reservation5 =  this.reservationFixture.createReservation(user, spot0C, today);

        Reservation reservation1t =  this.reservationFixture.createReservation(user, spotAA, tomorrow);
        Reservation reservation3t =  this.reservationFixture.createReservation(user, spotBA, tomorrow);
        Reservation reservation2t =  this.reservationFixture.createReservation(user, spotAB, tomorrow);
        Reservation reservation5t =  this.reservationFixture.createReservation(user, spot0C, tomorrow);
        Reservation reservation4t =  this.reservationFixture.createReservation(user, spotBB, tomorrow);


        mockMvc.perform(get("/api/me/reservations/current")
                .with(jwt()
                    .jwt(builder -> builder.subject(user.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()", equalTo(10)))
            .andExpect(jsonPath("$.content[*].userId", everyItem(is(user.getId().toString()))))
            .andExpect(jsonPath("$.content[*].id",
                contains(
                    reservation1.getId().toString(),
                    reservation2.getId().toString(),
                    reservation3.getId().toString(),
                    reservation4.getId().toString(),
                    reservation5.getId().toString(),

                    reservation1t.getId().toString(),
                    reservation2t.getId().toString(),
                    reservation3t.getId().toString(),
                    reservation4t.getId().toString(),
                    reservation5t.getId().toString()
                )
            ))
        ;
    }

    @Test
    void shouldReturnReservationsFromTodayAndFuture() throws Exception {
        LocalDate today = LocalDate.now(clock);
        LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
        LocalDate yesterday = today.minus(1, ChronoUnit.DAYS);

        Spot spot = this.spotFixture.createSpot();
        User user = this.userFixture.createUser();

        Reservation yesterdayReservation =  this.reservationFixture.createReservation(user, spot, yesterday);
        Reservation todayReservation =  this.reservationFixture.createReservation(user, spot, today);
        Reservation tomorrowReservation =  this.reservationFixture.createReservation(user, spot, tomorrow);

        mockMvc.perform(get("/api/me/reservations/current")
                .with(jwt()
                    .jwt(builder -> builder.subject(user.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()", equalTo(2)))
            .andExpect(jsonPath("$.content[*].userId", everyItem(is(user.getId().toString()))))
            .andExpect(jsonPath("$.content[*].id",
                contains(
                    todayReservation.getId().toString(),
                    tomorrowReservation.getId().toString()
                )
            ))
        ;
    }

    @Test
    void shouldReturnPaginatedReservations() throws Exception {
        LocalDate today = LocalDate.now(clock);

        Spot spot = this.spotFixture.createSpot();
        User user = this.userFixture.createUser();

        Reservation reservation11 =  this.reservationFixture.createReservation(user, spot, today);
        Reservation reservation21 =  this.reservationFixture.createReservation(user, spot, today.plusDays(2));
        Reservation reservation12 =  this.reservationFixture.createReservation(user, spot, today.plusDays(1));
        Reservation reservation22 =  this.reservationFixture.createReservation(user, spot, today.plusDays(3));

        mockMvc.perform(get("/api/me/reservations/current?size=2")
                .with(jwt()
                    .jwt(builder -> builder.subject(user.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()", equalTo(2)))
            .andExpect(jsonPath("$.content[*].userId", everyItem(is(user.getId().toString()))))
            .andExpect(jsonPath("$.content[*].id",
                contains(
                    reservation11.getId().toString(),
                    reservation12.getId().toString()
                )
            ))
        ;

        mockMvc.perform(get("/api/me/reservations/current?size=2&page=1")
                .with(jwt()
                    .jwt(builder -> builder.subject(user.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()", equalTo(2)))
            .andExpect(jsonPath("$.content[*].userId", everyItem(is(user.getId().toString()))))
            .andExpect(jsonPath("$.content[*].id",
                contains(
                    reservation21.getId().toString(),
                    reservation22.getId().toString()
                )
            ))
        ;
    }

    @Test
    void shouldThrowUnauthorizedWhenWithoutToken() throws Exception {
        mockMvc.perform(
                get("/api/me/reservations/current")
            )
            .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    void shouldDeleteReservationFromList() throws Exception {
        LocalDate today = LocalDate.now(clock);
        Spot spot = this.spotFixture.createSpot();
        User user = this.userFixture.createUser();

        Reservation reservation = this.reservationFixture.createReservation(user, spot, today);

        MvcResult result = mockMvc.perform(get("/api/me/reservations/current?size=2")
                .with(jwt()
                    .jwt(builder -> builder.subject(user.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()", equalTo(1)))
            .andReturn();
        ;

        String json = result.getResponse().getContentAsString();
        String reservationId = JsonPath.read(json, "$.content[0].id");
        assertThat(reservationId).isEqualTo(reservation.getId().toString());

        mockMvc.perform(delete("/api/reservation/%s".formatted(reservationId))
                .with(jwt()
                    .jwt(builder -> builder.subject(user.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
                .contentType(MediaType.APPLICATION_JSON)
            )

            // Then:
            .andExpect(status().isNoContent())
        ;

        mockMvc.perform(get("/api/me/reservations/current?size=2")
                .with(jwt()
                    .jwt(builder -> builder.subject(user.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()", equalTo(0)))
        ;


        mockMvc.perform(delete("/api/reservation/%s".formatted(reservationId))
                .with(jwt()
                    .jwt(builder -> builder.subject(user.getId().toString()))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                )
                .contentType(MediaType.APPLICATION_JSON)
            )

            // Then:
            .andExpect(status().isNotFound())
        ;



    }
}