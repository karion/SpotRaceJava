package pl.net.karion.SpotRacer.company.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import pl.net.karion.SpotRacer.company.fixture.CompanyFixture;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class CompanyRepositoryTest extends IntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyFixture companyFixture;

    @Test
    void shouldSaveActiveCompany() {
        Company company = new Company(
            "Januszex",
            true
        );

        Company savedCompany = companyRepository.saveAndFlush(company);

        assertThat(savedCompany).isNotNull();
        assertThat(savedCompany.getName()).isEqualTo("Januszex");
        assertThat(savedCompany.getNormalizedName()).isEqualTo("januszex");
        assertThat(savedCompany.isActive()).isTrue();
    }

    @Test
    void shouldNotSaveActiveCompanyWithSameName() {

        Company company = this.companyFixture.createCompany("Fakturex", true);

        Company newCompany = new Company(
            "FAKTUREX",
            true
        );

        assertThatThrownBy(()  -> companyRepository.saveAndFlush(newCompany))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldSaveCompanyWithSamaNameIfInactive() {
        Company company = this.companyFixture.createCompany("Szwagrex", false);

        Company newCompany = new Company(
            "Szwagrex",
            true
        );

        Company savedCompany = companyRepository.saveAndFlush(newCompany);

        assertThat(savedCompany).isNotNull();
        assertThat(savedCompany.getName()).isEqualTo("Szwagrex");
        assertThat(savedCompany.getNormalizedName()).isEqualTo("szwagrex");
        assertThat(savedCompany.isActive()).isTrue();
    }

    @Test
    void shouldSaveTwoInactiveCompanyWithSameName() {
        Company company = this.companyFixture.createCompany("Mirex", false);

        Company newCompany = new Company(
            "Mirex",
            false
        );

        Company savedCompany = companyRepository.saveAndFlush(newCompany);

        assertThat(savedCompany).isNotNull();
        assertThat(savedCompany.getName()).isEqualTo("Mirex");
        assertThat(savedCompany.getNormalizedName()).isEqualTo("mirex");
        assertThat(savedCompany.isActive()).isFalse();
    }

    @Test
    void shouldNotUpdateToActiveWhenSameName() {
        Company company = this.companyFixture.createCompany("Drutexpol", true);

        Company newCompany = new Company(
            "Drutexpol",
            false
        );

        Company savedCompany = companyRepository.saveAndFlush(newCompany);

        savedCompany.setActive(true);

        assertThatThrownBy(()  -> companyRepository.saveAndFlush(savedCompany))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}