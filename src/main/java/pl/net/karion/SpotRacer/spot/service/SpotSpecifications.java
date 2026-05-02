package pl.net.karion.SpotRacer.spot.service;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import pl.net.karion.SpotRacer.spot.model.Spot;

public class SpotSpecifications {


    public static Specification<Spot> hasNameOrLocationName(String search) {

        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.toLowerCase() + "%";

            var spotName = cb.like(cb.lower(root.get("name")), pattern);

            var locationJoin = root.join("location", JoinType.LEFT);
            var locationName = cb.like(cb.lower(locationJoin.get("name")), pattern);

            return cb.or(spotName, locationName);
        };
    }
}
