package pl.net.karion.SpotRacer.assignment.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID>, JpaSpecificationExecutor<Assignment> {
    @Query("""
        SELECT a FROM Assignment a
        WHERE a.spot.id = :spotId
        AND (
          (a.endDate IS NULL OR :start <= a.endDate)
          AND
          (:end IS NULL OR a.startDate <= :end)
        ) 
        AND (:excludeId IS NULL OR a.id <> :excludeId)
    """)
    List<Assignment> findOverlapping(
            @Param("spotId") UUID spotId,
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate,
            @Param("excludeId") UUID assignmentId
    );
}
