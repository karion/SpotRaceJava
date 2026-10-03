package pl.net.karion.SpotRacer.calendar.api;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.net.karion.SpotRacer.assignment.fixtures.AssignmentFixture;
import pl.net.karion.SpotRacer.reservation.fixtures.ReservationFixture;
import pl.net.karion.SpotRacer.spot.fixtures.LocationFixture;
import pl.net.karion.SpotRacer.spot.fixtures.SpotFixture;
import pl.net.karion.SpotRacer.spot.model.Location;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties =
    "spring.jpa.properties.hibernate.generate_statistics=true")
public class CalendarPerformanceTest extends IntegrationTest {

    @Autowired
    private AssignmentFixture assignmentFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private SpotFixture spotFixture;

    @Autowired
    private LocationFixture locationFixture;

    @Autowired
    private ReservationFixture reservationFixture;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Clock clock;

    @Autowired
    private EntityManagerFactory entityManagerFactory;


    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.of("Europe/Warsaw");
        LocalDate today = LocalDate.now(zone);
        Instant beforeRelease = today.atTime(6, 59).atZone(zone).toInstant();

        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(beforeRelease);
    }

    @Test
    void shouldMeasureDBLoad() throws Exception {
        User user = this.userFixture.createUser();

        long range = 10;
        List<Spot> spots = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            char locationLetter = (char) ('A' + i);

            Location location = this.locationFixture.createLocation(String.valueOf(locationLetter));

            for (int j = 0; j < range; j++) {
                String spotName = "%s_%d".formatted(locationLetter, j + 1);
                Spot spot = this.spotFixture.createSpot(spotName, location);
                spots.add(spot);
            }
        }

        for (int j = 0; j < range; j++) {
            String spotName = "%s_%d".formatted("x", j+1);
            Spot spot = this.spotFixture.createSpotWithoutLocation(spotName);
            spots.add(spot);
        }

        LocalDate today = LocalDate.now(this.clock);

        List<Spot> shuffledSpots = new ArrayList<Spot>(spots);
        Collections.shuffle(shuffledSpots);

        this.assignmentFixture.createAssignment(user, shuffledSpots.get(0), today.minusDays(5).toString(), null, null);

        this.reservationFixture.createReservation(user, shuffledSpots.get(0), today.plusDays(5));
        this.reservationFixture.createReservation(user, shuffledSpots.get(2), today.plusDays(1));


        for (int i = 0; i < 20; i++) {
            User randomUser = this.userFixture.createUser();

            this.assignmentFixture.createAssignment(
                randomUser,
                shuffledSpots.get(10 + i),
                today.minusDays(5).toString(),
                null,
                null
            );
            this.assignmentFixture.createAssignment(
                randomUser,
                shuffledSpots.get(60 + i),
                today.plusDays(1).toString(),
                today.plusDays(6).toString(),
                null
            );

            this.reservationFixture.createReservation(randomUser, shuffledSpots.get(10 + i), today.plusDays(3));
            this.reservationFixture.createReservation(randomUser, shuffledSpots.get(85 + i), today.plusDays(1));
        }

        Statistics statistics =
            entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        statistics.clear();

        long start = System.nanoTime();

        mockMvc.perform(
            get("/api/calendar")
            .with(jwt()
                .jwt(builder -> builder.subject(user.getId().toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
            )
        )
        .andExpect(status().isOk())
        ;

        long stop = System.nanoTime();

        long statementCount = statistics.getPrepareStatementCount();

        assertThat(statementCount).isLessThanOrEqualTo(3);
    }
}
