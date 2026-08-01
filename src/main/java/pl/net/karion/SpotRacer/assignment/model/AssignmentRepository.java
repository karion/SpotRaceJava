package pl.net.karion.SpotRacer.assignment.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID>, JpaSpecificationExecutor<Assignment> {

    @Query("""
    select a
    from Assignment a
    where a.spot.id = :spotId
        and a.startDate <= :reservationDate
        and (
            a.endDate is null
            or a.endDate >= :reservationDate
        )
""")
    Optional<Assignment> findActiveAssignment(UUID spotId, LocalDate reservationDate);


    @Query("""
    select a
    from Assignment a
    where a.startDate <= :dateTo
          and (a.endDate is null or a.endDate >= :dateFrom)
""")
    List<Assignment> findActiveAssignmentsBetween(LocalDate dateFrom, LocalDate dateTo);
}
