package pl.net.karion.SpotRacer.spot.service;

import org.springframework.data.jpa.domain.Specification;
import pl.net.karion.SpotRacer.spot.model.Location;
import pl.net.karion.SpotRacer.spot.model.Spot;

public class SpotSpecifications {

    public static Specification<Spot> hasName(String searchText) {
        return (root, query, cb) ->
                searchText == null || searchText.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("name")), "%" + searchText.toLowerCase() + "%");
    }
}
