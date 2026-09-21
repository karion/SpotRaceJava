package pl.net.karion.SpotRacer.reservation.api;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import pl.net.karion.SpotRacer.assignment.fixtures.AssignmentFixture;
import pl.net.karion.SpotRacer.assignment.model.Assignment;
import pl.net.karion.SpotRacer.reservation.exception.ReservationAlreadyTakenException;
import pl.net.karion.SpotRacer.reservation.exception.ReservationNotFoundException;
import pl.net.karion.SpotRacer.reservation.exception.ReservationTooFarInFutureException;
import pl.net.karion.SpotRacer.reservation.exception.ReservationWithAssignmentNotReleasedYetException;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    private ReservationFixture reservationFixture;

    @Autowired
    private AssignmentFixture assignmentFixture;

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

    @Test
    void shouldReturnConflictWhenSpotIsAlreadyReservedForDate() throws Exception {
        Spot spot = this.spotFixture.createSpot("Spot for reservation");
        User user = this.userFixture.createUser(UserFixture.randomEmail(), "Sylwia", "Szybka");
        User otherUser = this.userFixture.createUser(UserFixture.randomEmail(), "Paulina", "Powolna");

        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);

        Reservation oldReservation = reservationFixture.createReservation(
            user, spot,  tomorrow
        );

        String body = this.createBody(otherUser.getId(), spot.getId(), tomorrow.toString());

        // When: POST /api/reservation na istniejącej rezerwacji

        mockMvc.perform(post("/api/reservation")
                .with(this.jwtFor(otherUser.getId(), Role.USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )

            // Then: status 409.
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(ReservationAlreadyTakenException.RESERVATION_ALREADY_TAKEN))
        ;
    }

    @Test
    void shouldCreateOnlyOneReservationWhenTwoUsersReserveSameSpotConcurrently() throws Exception {
        Spot spot = this.spotFixture.createSpot("Spot for reservation");
        User user = this.userFixture.createUser(UserFixture.randomEmail(), "Sylwia", "Szybka");
        User otherUser = this.userFixture.createUser(UserFixture.randomEmail(), "Paulina", "Powolna");

        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);

        String body1 = this.createBody(user.getId(), spot.getId(), tomorrow.toString());
        String body2 = this.createBody(otherUser.getId(), spot.getId(), tomorrow.toString());

        Callable<MvcResult> requestA = () -> {
            return mockMvc.perform(post("/api/reservation")
                .with(this.jwtFor(user.getId(), Role.USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body1)
            )
            .andReturn()
            ;
        };

        Callable<MvcResult> requestB = () -> {
            return mockMvc.perform(post("/api/reservation")
                .with(this.jwtFor(otherUser.getId(), Role.USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body2)
            )
            .andReturn()
            ;
        };

        Specification<Reservation> spec = (root, query, cb) ->
            cb.and(
                cb.equal(root.get("spot"), spot),
                cb.equal(root.get("date"), tomorrow)
            );

        CyclicBarrier barrier = new CyclicBarrier(2);

        MethodInterceptor interceptor = invocation -> {
            boolean matchingCall =
                invocation.getMethod().getName().equals("existsBySpotIdAndDate")
                    && invocation.getArguments().length == 2
                    && spot.getId().equals(invocation.getArguments()[0])
                    && tomorrow.equals(invocation.getArguments()[1]);

            // Wykonanie oryginalnej metody repozytorium.
            Object result = invocation.proceed();

            if (matchingCall) {
                barrier.await(5, TimeUnit.SECONDS);
            }

            return result;
        };

        Advised repositoryProxy = (Advised) reservationRepository;
        repositoryProxy.addAdvice(0, interceptor);


        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<MvcResult> futureA = executor.submit(requestA);
            Future<MvcResult> futureB = executor.submit(requestB);

            try {
                MvcResult resultA = futureA.get(10, TimeUnit.SECONDS);
                MvcResult resultB = futureB.get(10, TimeUnit.SECONDS);

                List<MockHttpServletResponse> responses = List.of(
                    resultA.getResponse(),
                    resultB.getResponse()
                );

                List<Reservation> reservations = this.reservationRepository.findAll(spec);
                assertThat(reservations).hasSize(1);
                assertThat(responses.stream().map(MockHttpServletResponse::getStatus))
                    .containsExactlyInAnyOrder(409, 201);
                UUID userIdInDb = reservations.getFirst().getUser().getId();

                if (resultA.getResponse().getStatus() == 201) {
                    jsonPath("$.userId")
                        .value(user.getId().toString())
                        .match(resultA);
                    assertThat(user.getId()).isEqualTo(userIdInDb);
                } else if (resultB.getResponse().getStatus() == 201) {
                    jsonPath("$.userId")
                        .value(otherUser.getId().toString())
                        .match(resultB);
                    assertThat(otherUser.getId()).isEqualTo(userIdInDb);
                }
            } finally {
                futureA.cancel(true);
                futureB.cancel(true);
            }
        } finally {
            repositoryProxy.removeAdvice(interceptor);
        }
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonexistentReservation() throws Exception {
        // When: DELETE /api/reservation/%id

        mockMvc.perform(delete("/api/reservation/%s".formatted(UUID.randomUUID().toString()))
                .with(this.jwtFor(UUID.randomUUID(), Role.USER))
                .contentType(MediaType.APPLICATION_JSON)
            )

            // Then:
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(ReservationNotFoundException.RESERVATION_NOT_FOUND))
        ;
    }

    @Test
    void shouldForbidDeletingAnotherUsersReservation() throws Exception {

        User user = this.userFixture.createUser();
        User otherUser = this.userFixture.createUser();
        Spot spot =  this.spotFixture.createSpot("Spot for reservation");

        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);

        Reservation reservation = reservationFixture.createReservation(
            user,
            spot,
            tomorrow
        );

        // When: DELETE /api/reservation/%id

        mockMvc.perform(delete("/api/reservation/%s".formatted(reservation.getId().toString()))
                .with(this.jwtFor(otherUser.getId(), Role.USER))
                .contentType(MediaType.APPLICATION_JSON)
            )

            // Then:
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value(RESERVATION_REQUIRES_SELF_OR_ADMIN_MESSAGE))
        ;

        assertThat(reservationRepository.existsById(reservation.getId())).isTrue();
    }

    @Test
    void shouldReturnBadRequestWhenReservationDateExceedsAllowedWindow() throws Exception {

        User user = this.userFixture.createUser();
        Spot spot =  this.spotFixture.createSpot("Spot for reservation");

        LocalDate farFuture = LocalDate.now(clock).plusDays(100);

        String body = this.createBody(user.getId(), spot.getId(), farFuture.toString());

        // When: POST /api/reservation

        mockMvc.perform(post("/api/reservation")
                .with(this.jwtFor(user.getId(), Role.USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )

            // Then:
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(ReservationTooFarInFutureException.RESERVATION_TOO_FAR_IN_FUTURE))
        ;
    }

    @Test
    void shouldReturnConflictWhenAssignedSpotIsNotReleasedYet() throws Exception {

        User user = this.userFixture.createUser();
        User otherUser = this.userFixture.createUser();
        Spot spot =  this.spotFixture.createSpot("Spot for reservation");

        LocalDate today = LocalDate.now(clock);

        Assignment assignment = this.assignmentFixture.createAssignment(
            user,
            spot,
            today.toString(),
            null,
            ""
        );

        String body = this.createBody(otherUser.getId(), spot.getId(), today.toString());

        // When: POST /api/reservation

        mockMvc.perform(post("/api/reservation")
                .with(this.jwtFor(otherUser.getId(), Role.USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )

            // Then:
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(ReservationWithAssignmentNotReleasedYetException.RESERVATION_WITH_ASSIGNMENT_NOT_RELEASED_YET))
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

    private RequestPostProcessor jwtFor(UUID userId, Role role) {
        return jwt()
            .jwt(builder -> builder.subject(userId.toString()))
            .authorities(new SimpleGrantedAuthority("ROLE_%s".formatted(role.name())))
        ;
    }
}
