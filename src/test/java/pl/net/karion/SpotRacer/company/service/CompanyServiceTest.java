package pl.net.karion.SpotRacer.company.service;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pl.net.karion.SpotRacer.company.api.CompanyRequest;
import pl.net.karion.SpotRacer.company.api.CompanyResponse;
import pl.net.karion.SpotRacer.company.exception.CompanyNameCollision;
import pl.net.karion.SpotRacer.company.exception.CompanyNotFound;
import pl.net.karion.SpotRacer.company.model.Company;
import pl.net.karion.SpotRacer.company.model.CompanyRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    CompanyService companyService;

    @Test
    void shouldCreateCompany() {
        when(companyRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        CompanyRequest request = new CompanyRequest(
            "Januszex  Max",
            true
        );

        CompanyResponse response = companyService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Januszex Max");
        assertThat(response.normalizedName()).isEqualTo("januszex max");
        assertThat(response.active()).isEqualTo(true);
    }

    public static Stream<Arguments> wrongNames() {
        return Stream.of(

            // overlap
            Arguments.of((String)null),
            Arguments.of(""),
            Arguments.of("  "),
            Arguments.of("vLorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc molestie, turpis sed dignissim vehicula, tellus nulla auctor ipsum, vitae ullamcorper arcu enim egestas nibh. Duis varius iaculis tempor. Class aptent taciti sociosqu ad litora massa nunc.")
        );
    }

    @ParameterizedTest
    @MethodSource("wrongNames")
    void shouldFailOnIncorrectName(String name) {
        CompanyRequest request = new CompanyRequest(
            name,
            false
        );

        assertThatThrownBy(() -> companyService.create(request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailOnCompanyNameCollision() {
        SQLException sqlError = new SQLException("konflikt unikalności");

        ConstraintViolationException constraintError = new ConstraintViolationException(
            "konflikt unikalności",
            sqlError,
            "uq_company__normalized_name_active"
        );

        DataIntegrityViolationException repositoryError = new DataIntegrityViolationException(
            "konflikt unikalności",
            constraintError
        );
        when(companyRepository.saveAndFlush(any())).thenThrow(repositoryError);

        CompanyRequest request = new CompanyRequest(
            "Januszex  Max",
            true
        );

        assertThatThrownBy(() -> companyService.create(request))
            .isInstanceOf(CompanyNameCollision.class);
    }


    @Test
    void shouldThrowOtherExceptionsUp() {
        SQLException sqlError = new SQLException("konflikt other_exception");

        ConstraintViolationException constraintError = new ConstraintViolationException(
            "konflikt other_exception",
            sqlError,
            "other_exception"
        );

        DataIntegrityViolationException repositoryError = new DataIntegrityViolationException(
            "other_exception",
            constraintError
        );
        when(companyRepository.saveAndFlush(any())).thenThrow(repositoryError);

        CompanyRequest request = new CompanyRequest(
            "Januszex  Max",
            true
        );

        assertThatThrownBy(() -> companyService.create(request))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldUpdateCompany() {
        Company company = new Company(
            "Dariuszex",
            true
        );

        UUID companyId = company.getId();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        when(companyRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        CompanyRequest request = new CompanyRequest(
            "Albatrox",
            true
        );

        CompanyResponse response = companyService.update(companyId, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(companyId);
        assertThat(response.name()).isEqualTo("Albatrox");
        assertThat(response.normalizedName()).isEqualTo("albatrox");
        assertThat(response.active()).isEqualTo(true);
    }

    @Test
    void shouldActivateCompany() {
        Company company = new Company(
            "Niepracuś",
            false
        );

        UUID companyId = company.getId();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        when(companyRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        CompanyRequest request = new CompanyRequest(
            "Niepracuś",
            true
        );

        CompanyResponse response = companyService.update(companyId, request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Niepracuś");
        assertThat(response.normalizedName()).isEqualTo("niepracuś");
        assertThat(response.active()).isEqualTo(true);
    }

    @Test
    void shouldThrowNotFoundOnIncorrectUuid() {
        UUID companyId = UUID.randomUUID();

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        CompanyRequest request = new CompanyRequest(
            "Mgielnik",
            true
        );

        assertThatThrownBy(() -> companyService.update(companyId, request))
            .isInstanceOf(CompanyNotFound.class);
    }

    @ParameterizedTest
    @MethodSource("wrongNames")
    void shouldThrowErrorOnWrongNames(String name) {

        Company company = new Company(
            "Niepracuś",
            false
        );

        UUID companyId = company.getId();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompanyRequest request = new CompanyRequest(
            name,
            true
        );

        assertThatThrownBy(() -> companyService.update(companyId, request))
            .isInstanceOf(IllegalArgumentException.class);

        verify(companyRepository, never()).saveAndFlush(any(Company.class));

        assertThat(company.getName()).isEqualTo("Niepracuś");
        assertThat(company.getNormalizedName()).isEqualTo("niepracuś");
        assertThat(company.isActive()).isFalse();
    }

    @Test
    void shouldThrowNameConflictOnUpdate() {
        Company company = new Company(
            "Niepracuś",
            true
        );

        UUID companyId = company.getId();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompanyRequest request = new CompanyRequest(
            "Mgielnik",
            true
        );

        SQLException sqlError = new SQLException("konflikt unikalności");

        ConstraintViolationException constraintError = new ConstraintViolationException(
            "konflikt unikalności",
            sqlError,
            "uq_company__normalized_name_active"
        );

        DataIntegrityViolationException repositoryError = new DataIntegrityViolationException(
            "konflikt unikalności",
            constraintError
        );
        when(companyRepository.saveAndFlush(any())).thenThrow(repositoryError);

        assertThatThrownBy(() -> companyService.update(companyId, request))
            .isInstanceOf(CompanyNameCollision.class);
    }

    @Test
    void shouldThrowOtherExceptionThenNameConflictOnUpdate() {
        Company company = new Company(
            "Niepracuś",
            true
        );

        UUID companyId = company.getId();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompanyRequest request = new CompanyRequest(
            "Mgielnik",
            true
        );

        SQLException sqlError = new SQLException("konflikt unikalności");

        ConstraintViolationException constraintError = new ConstraintViolationException(
            "konflikt unikalności",
            sqlError,
            "other_voliation"
        );

        DataIntegrityViolationException repositoryError = new DataIntegrityViolationException(
            "konflikt unikalności",
            constraintError
        );
        when(companyRepository.saveAndFlush(any())).thenThrow(repositoryError);

        assertThatThrownBy(() -> companyService.update(companyId, request))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindById() {
        Company company = new Company(
            "Istniex",
            true
        );

        UUID companyId = company.getId();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompanyResponse response = companyService.getById(companyId);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Istniex");
        assertThat(response.normalizedName()).isEqualTo("istniex");
        assertThat(response.active()).isEqualTo(true);
    }

    @Test
    void shouldThrowNotFoundOnFindById() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getById(companyId))
            .isInstanceOf(CompanyNotFound.class);
    }

    @Test
    void shouldGetAll() {
        Company company1 = new Company("Pierwix", true);
        Company company2 = new Company("Drugix", true);
        Pageable pageable = PageRequest.of(0, 2);
        List<Company> companies = List.of(company1, company2);

        Page<Company> page = new PageImpl<>(
            companies,
            pageable,
            10
        );

        when(companyRepository.findAll(
            ArgumentMatchers.<Specification<Company>>any(),
            eq(pageable)
        )).thenReturn(page);
        Page<CompanyResponse> response = companyService.getAll(null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent().size()).isEqualTo(2);
        assertThat(response.getContent().stream().map((CompanyResponse cr) -> cr.id()))
            .containsExactlyElementsOf(List.of(company1.getId(), company2.getId()));
        assertThat(response.getTotalElements()).isEqualTo(10);
        assertThat(response.getTotalPages()).isEqualTo(5);
    }

    @Test
    void shouldReturnEmptyGetAll() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Company> companies = List.of();

        Page<Company> page = new PageImpl<>(
            companies,
            pageable,
            companies.size()
        );

        when(companyRepository.findAll(
            ArgumentMatchers.<Specification<Company>>any(),
            eq(pageable)
        )).thenReturn(page);
        Page<CompanyResponse> response = companyService.getAll(null, pageable);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(0);
    }
}