package pl.net.karion.SpotRacer.company.service;

import org.springframework.data.jpa.domain.Specification;
import pl.net.karion.SpotRacer.company.model.Company;

public class CompanySpecification {
    public static Specification<Company> hasName(String search) {
        return (root, query, cb) ->
            search == null || search.isBlank()
                ? null
                : cb.like(root.get("normalizedName"), "%" + search.toLowerCase() + "%");
    }
}
