package pl.net.karion.SpotRacer.reservation.fixtures;

import org.springframework.stereotype.Component;
import pl.net.karion.SpotRacer.reservation.model.Reservation;
import pl.net.karion.SpotRacer.reservation.model.ReservationRepository;
import pl.net.karion.SpotRacer.spot.fixtures.SpotFixture;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class ReservationFixture {

    private final ReservationRepository reservationRepository;
    private final UserFixture userFixture;
    private final SpotFixture spotFixture;

    public ReservationFixture(
        ReservationRepository reservationRepository,
        UserFixture userFixture,
        SpotFixture spotFixture
    ) {
        this.reservationRepository = reservationRepository;
        this.userFixture = userFixture;
        this.spotFixture = spotFixture;
    }
    public Reservation createReservation() {
        return this.createReservation(
            userFixture.createUser(),
            spotFixture.createSpot(),
            LocalDate.now()
        );
    }

    public Reservation createReservation(
        User user,
        Spot spot,
        LocalDate date
    ) {
        Reservation reservation = new Reservation(
            UUID.randomUUID(),
            user,
            spot,
            date
        );

        return  reservationRepository.save(reservation);
    }
}
