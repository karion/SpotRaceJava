package pl.net.karion.SpotRacer.assignment.service;

import org.springframework.data.jpa.domain.Specification;
import pl.net.karion.SpotRacer.assignment.model.Assignment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

public class AssignmentSpecification {

    public static Specification<Assignment> overlapping(
            UUID spotId,
            LocalDate startDate,
            LocalDate endDate,
            UUID excludeId
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // spot
            predicates.add(
                cb.equal(root.get("spot").get("id"), spotId)
            );

            // (a.endDate IS NULL OR a.endDate >= :start)
            predicates.add(
                cb.or(
                    cb.isNull(root.get("endDate")),
                    cb.greaterThanOrEqualTo(
                        root.get("endDate"),
                        startDate
                    )
                )
            );

            // (:end IS NULL OR a.startDate <= :end)
            if (endDate != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(
                        root.get("startDate"),
                        endDate
                    )
                );
            }

            // a.id <> :excludeId
            if (excludeId != null) {
                predicates.add(
                        cb.notEqual(root.get("id"), excludeId)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
