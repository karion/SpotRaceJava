package pl.net.karion.SpotRacer.reservation.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, UUID>, JpaSpecificationExecutor<Reservation> {
    boolean existsBySpotIdAndDate(UUID spotId, LocalDate date);

    @Query("""
        select r
        from Reservation r
        where r.date >= :dateFrom
          and r.date <= :dateTo
        order by date
        """)
    List<Reservation> findReservationsBetween(
            LocalDate dateFrom,
            LocalDate dateTo
    );
}
