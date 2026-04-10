package pl.net.karion.SpotRacer.user.service;

import org.springframework.data.jpa.domain.Specification;
import pl.net.karion.SpotRacer.user.model.User;

public class UserSpecifications {

    public static Specification<User> hasFirstname(String searchText) {
        return (root, query, cb) ->
                searchText == null || searchText.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("firstname")), "%" + searchText.toLowerCase() + "%");
    }

    public static Specification<User> hasLastname(String searchText) {
        return (root, query, cb) ->
                searchText == null || searchText.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("lastname")), "%" + searchText.toLowerCase() + "%");
    }

    public static Specification<User> hasEmail(String searchText) {
        return (root, query, cb) ->
                searchText == null || searchText.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("email")), "%" + searchText.toLowerCase() + "%");
    }
}
