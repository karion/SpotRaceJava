package pl.net.karion.SpotRacer.reservation.model;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReservationRepository extends JpaRepository<Reservation, UUID>, JpaSpecificationExecutor<Reservation> {
    boolean existsBySpotIdAndDate(UUID spotId, LocalDate date);
}
