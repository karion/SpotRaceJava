package pl.net.karion.SpotRacer.company.service;

import pl.net.karion.SpotRacer.company.api.CompanyResponse;
import pl.net.karion.SpotRacer.company.model.Company;

public class CompanyMapper {

    public static CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
            company.getId(),
            company.getName(),
            company.getNormalizedName(),
            company.isActive()
        );
    }
}
