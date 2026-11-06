package pl.net.karion.SpotRacer.company.fixture;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.stereotype.Component;
import pl.net.karion.SpotRacer.company.model.Company;
import pl.net.karion.SpotRacer.company.model.CompanyRepository;

@Component
public class CompanyFixture {

    private CompanyRepository companyRepository;

    public CompanyFixture (CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(String name, boolean active) {
        Company company = new Company(name, active);

        return companyRepository.saveAndFlush(company);
    }
}
