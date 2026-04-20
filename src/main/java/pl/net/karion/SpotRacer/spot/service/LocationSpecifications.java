package pl.net.karion.SpotRacer.spot.service;

import org.springframework.data.jpa.domain.Specification;
import pl.net.karion.SpotRacer.spot.model.Location;

public class LocationSpecifications {

    public static Specification<Location> hasName(String searchText) {
        return (root, query, cb) ->
                searchText == null || searchText.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("name")), "%" + searchText.toLowerCase() + "%");
    }

    public static Specification<Location> hasDescription(String searchText) {
        return (root, query, cb) ->
                searchText == null || searchText.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("description")), "%" + searchText.toLowerCase() + "%");
    }
}
