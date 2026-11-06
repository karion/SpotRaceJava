package pl.net.karion.SpotRacer.company.api.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.net.karion.SpotRacer.company.exception.CompanyNameCollision;
import pl.net.karion.SpotRacer.company.exception.CompanyNotFound;
import pl.net.karion.SpotRacer.company.fixture.CompanyFixture;
import pl.net.karion.SpotRacer.company.model.Company;
import pl.net.karion.SpotRacer.support.IntegrationTest;

import static org.hamcrest.Matchers.*;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CompanyControllerTest extends IntegrationTest {

    @Autowired
    private CompanyFixture companyFixture;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateCompany() throws Exception {
        String body = this.createBody(
            "Firmex",
            true
        );

        mockMvc.perform(post("/api/company")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Firmex"))
            .andExpect(jsonPath("$.normalizedName").value("firmex"))
            .andExpect(jsonPath("$.active").value(true))
        ;
    }

    @Test
    void shouldThrowConflictOnSameName() throws Exception {

        Company company = this.companyFixture.createCompany(
            "Samex",
            true
        );

        String body = this.createBody(
            "samex",
            true
        );

        mockMvc.perform(post("/api/company")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(CompanyNameCollision.COMPANY_NAME_COLLISION_MSG))
        ;
    }

    public static Stream<Arguments> wrongNames() {
        return Stream.of(
            Arguments.of((String)null),
            Arguments.of(""),
            Arguments.of("  "),
            Arguments.of("vLorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc molestie, turpis sed dignissim vehicula, tellus nulla auctor ipsum, vitae ullamcorper arcu enim egestas nibh. Duis varius iaculis tempor. Class aptent taciti sociosqu ad litora massa nunc.")
        );
    }

    @ParameterizedTest
    @MethodSource("wrongNames")
    void shouldThrowBadRequestOnWrongName(final String name) throws Exception {
        String body = this.createBody(
            name,
            true
        );

        mockMvc.perform(post("/api/company")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void shouldThrowUnauthorisedOnCreate() throws Exception {
        String body = this.createBody(
            "Tajniak",
            true
        );

        mockMvc.perform(post("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isUnauthorized())
        ;
    }

    public static Stream<Arguments> wrongRoles() {
        return Stream.of(
            Arguments.of("USER"),
            Arguments.of("COMPANY_ADMIN")
        );
    }

    @ParameterizedTest
    @MethodSource("wrongRoles")
    void shouldThrowForbidden(String role) throws Exception {
        String body = this.createBody(
            "Oszustix",
            true
        );

        mockMvc.perform(post("/api/company")
                .with(user("admin").roles(role))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldReturnCompanyById() throws Exception {
        Company company = this.companyFixture.createCompany(
            "Budex",
            true
        );

        mockMvc.perform(get("/api/company/%s".formatted(company.getId()))
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(company.getId().toString()))
            .andExpect(jsonPath("$.name").value("Budex"))
            .andExpect(jsonPath("$.normalizedName").value("budex"))
            .andExpect(jsonPath("$.active").value(true))
        ;
    }

    @Test
    void shouldThrowNotFoundOnGetById() throws Exception {
        mockMvc.perform(get("/api/company/%s".formatted(UUID.randomUUID()))
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(CompanyNotFound.COMPANY_NOT_FOUND))
        ;
    }

    @ParameterizedTest
    @MethodSource("wrongRoles")
    void shouldThrowForbiddenOnGetById(String role) throws Exception {
        Company company = this.companyFixture.createCompany(
            "Budex-%s".formatted(role),
            true
        );

        mockMvc.perform(get("/api/company/%s".formatted(company.getId()))
                .with(user("admin").roles(role))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldThrowUnauthorizedOnGetById() throws Exception {
        Company company = this.companyFixture.createCompany(
            "Ogrix",
            true
        );

        mockMvc.perform(get("/api/company/%s".formatted(company.getId()))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    void shouldReturnPaginatedCompanies() throws Exception {
        this.companyFixture.createCompany(
            "Listaix",
            true
        );
        this.companyFixture.createCompany(
            "Listaix-2",
            true
        );
        this.companyFixture.createCompany(
            "Listaix-3",
            true
        );
        mockMvc.perform(get("/api/company?size=2&page=0")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(equalTo(2)))
            .andExpect(jsonPath("$.page.totalElements").value(greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.page.totalPages").value(greaterThan(1)))
        ;
    }

    @Test
    void shouldSearchByNamePaginatedCompanies() throws Exception {
        String randomString = UUID.randomUUID().toString().substring(0, 8);

        Company company1 = this.companyFixture.createCompany(
            "Gamma1-" + randomString,
            true
        );

        Company company2 = this.companyFixture.createCompany(
            "Gamma2-" + randomString,
            true
        );

        mockMvc.perform(get("/api/company?search=%s&size=2&page=0".formatted(randomString))
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(equalTo(2)))
            .andExpect(jsonPath("$.page.totalElements").value(equalTo(2)))
            .andExpect(jsonPath("$.page.totalPages").value(equalTo(1)))
            .andExpect(jsonPath("$.content[*].id",
                containsInAnyOrder(
                    company1.getId().toString(),
                    company2.getId().toString()
                )
            ))
        ;
    }

    @Test
    void shouldThrowUnauthorisedOnPaginatedCompanies() throws Exception {

        mockMvc.perform(get("/api/company")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized())
        ;
    }

    @ParameterizedTest
    @MethodSource("wrongRoles")
    void shouldThrowForbiddenOnPaginatedCompanies(String role) throws Exception {

        mockMvc.perform(get("/api/company")
                .with(user("user").roles(role))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldUpdateCompany() throws Exception {
        Company company = this.companyFixture.createCompany(
            "Nieaktualix",
            true
        );
        String body = this.createBody(
            "Aktualix",
            true
        );
        mockMvc.perform(put("/api/company/%s".formatted(company.getId()))
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(company.getId().toString()))
            .andExpect(jsonPath("$.name").value("Aktualix"))
            .andExpect(jsonPath("$.normalizedName").value("aktualix"))
            .andExpect(jsonPath("$.active").value(true))
        ;

        mockMvc.perform(get("/api/company/%s".formatted(company.getId()))
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(company.getId().toString()))
            .andExpect(jsonPath("$.name").value("Aktualix"))
            .andExpect(jsonPath("$.normalizedName").value("aktualix"))
            .andExpect(jsonPath("$.active").value(true))
        ;
    }

    @Test
    void shouldThrowBadRequestWithoutActiveOnUpdateCompany() throws Exception {
        Company company = this.companyFixture.createCompany(
            "Nieaktivix",
            true
        );
        String body = "{\"name\": \"Bez Aktivix\"}";

        mockMvc.perform(put("/api/company/%s".formatted(company.getId()))
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void shouldThrowNotFoundOnUpdateCompany() throws Exception {
        String body = this.createBody(
            "Nicosix",
            true
        );
        mockMvc.perform(put("/api/company/%s".formatted(UUID.randomUUID().toString()))
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
        )
        .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldThrowConflictWithConflictingCompanyName() throws Exception {
        this.companyFixture.createCompany(
            "Blokuix",
            true
        );

        Company company = this.companyFixture.createCompany(
            "Innonix",
            true
        );

        String body = this.createBody(
            "blokuix ",
            true
        );

        mockMvc.perform(put("/api/company/%s".formatted(company.getId()))
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(CompanyNameCollision.COMPANY_NAME_COLLISION_MSG))
            ;
    }

    @ParameterizedTest
    @MethodSource("wrongNames")
    void shouldThrowBadRequestOnWrongNameOnUpdate(final String name) throws Exception {
        Company company = this.companyFixture.createCompany(
            "Pustotix",
            false
        );
        String body = this.createBody(
            name,
            true
        );

        mockMvc.perform(put("/api/company/%s".formatted(company.getId()))
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isBadRequest())
        ;
    }

    @ParameterizedTest
    @MethodSource("wrongRoles")
    void shouldThrowForbiddenOnUpdate(String role) throws Exception {
        Company company = this.companyFixture.createCompany(
            "Aktualizatex-%s".formatted(role),
            true
        );

        String body = this.createBody("No-No", false);

        mockMvc.perform(put("/api/company/%s".formatted(company.getId()))
                .with(user("admin").roles(role))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldThrowUnauthorisedOnUpdate() throws Exception {

        mockMvc.perform(put("/api/company/%s".formatted(UUID.randomUUID().toString()))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized())
        ;
    }

    private String createBody(String companyName, boolean active) {
        return """
        {
          "name": %s,
          "active": %s
        }
        """.formatted(
            companyName == null ? null : "\"%s\"".formatted(companyName),
            active ? "true" : "false"
        );
    }
}