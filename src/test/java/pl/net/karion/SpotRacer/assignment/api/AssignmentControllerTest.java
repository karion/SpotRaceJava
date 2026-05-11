package pl.net.karion.SpotRacer.assignment.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.ResultMatcher;
import pl.net.karion.SpotRacer.assignment.fixtures.AssignmentFixture;
import pl.net.karion.SpotRacer.assignment.model.Assignment;
import pl.net.karion.SpotRacer.spot.fixtures.SpotFixture;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;

import java.util.UUID;
import java.util.stream.Stream;

public class AssignmentControllerTest  extends IntegrationTest {

    @Autowired
    private AssignmentFixture assignmentFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private SpotFixture spotFixture;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAssignment() throws Exception {

        Spot spot = this.spotFixture.createSpot("Spot create assignment");
        User user = this.userFixture.createUser(
                UserFixture.randomEmail(),
                "Hania",
                "Przypisówna"
        );

        String body = this.createBody(
                user.getId(),
                spot.getId(),
                "2030-01-01",
                "2030-01-10",
                "Test: AssignmentFixture.createAssignment"
        );

        mockMvc.perform(post("/api/assignment")
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                )

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spotId").value(spot.getId().toString()))
                .andExpect(jsonPath("$.spotName").value("Spot create assignment"))
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.userName").value("Hania Przypisówna"))
                .andExpect(jsonPath("$.startDate").value("2030-01-01"))
                .andExpect(jsonPath("$.endDate").value("2030-01-10"))
                .andExpect(jsonPath("$.note").value("Test: AssignmentFixture.createAssignment"))
        ;
    }

    @Test
    void shouldThrowForbiddenOnUserWhenCreateAssignment() throws Exception {

        String body = this.createBody(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "2030-01-01",
                "2030-01-10",
                "Test: AssignmentFixture.createAssignment"
        );

        mockMvc.perform(post("/api/assignment")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )

                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldThrowUnauthorizedWhenCreateAssignmentWithoutUser() throws Exception {

        String body = this.createBody(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "2030-01-01",
                "2030-01-10",
                "Test: AssignmentFixture.createAssignment"
        );

        mockMvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )

                .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    void shouldThrowsUserNotFoundWhenCreatingAssignmentWithRandomUserUuid() throws Exception {

        String body = this.createBody(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "2030-01-01",
                "2030-01-10",
                "Test: AssignmentFixture.createAssignment"
        );

        mockMvc.perform(post("/api/assignment")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )

                .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldThrowsSpotNotFoundWhenCreatingAssignmentWithRandomSpotUuid() throws Exception {

        User user = this.userFixture.createUser();

        String body = this.createBody(
                user.getId(),
                UUID.randomUUID(),
                "2030-01-01",
                "2030-01-10",
                "Test: AssignmentFixture.createAssignment"
        );

        mockMvc.perform(post("/api/assignment")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )

                .andExpect(status().isNotFound())
        ;
    }

    public static Stream<Arguments> overlapCases() {
        return Stream.of(

            // overlap
            Arguments.of(
                "2030-01-01",
                "2030-01-10",
                "2030-01-05",
                null,
                status().isConflict()
            ),

            Arguments.of(
                "2030-01-01",
                null,
                "2030-01-05",
                null,
                status().isConflict()
            ),

            // touching boundary -> conflict
            Arguments.of(
                "2030-01-01",
                "2030-01-10",
                "2030-01-10",
                "2030-01-15",
                status().isConflict()
            ),

            // B+1 -> Created
            Arguments.of(
                "2030-01-01",
                "2030-01-10",
                "2030-01-11",
                "2030-01-15",
                status().isCreated()
            ),

            // A-5 A -> Conflict
            Arguments.of(
                "2030-01-10",
                null,
                "2030-01-01",
                "2030-01-10",
                status().isConflict()
            ),

            // A-5 A-1 -> Created
            Arguments.of(
                "2030-01-10",
                null,
                "2030-01-01",
                "2030-01-09",
                status().isCreated()
            )
        );
    }

    @ParameterizedTest
    @MethodSource("overlapCases")
    void shouldDetectOverlap(
        String existingStart,
        String existingEnd,
        String newStart,
        String newEnd,
        ResultMatcher expectedStatus
    ) throws Exception {

        User user = this.userFixture.createUser();
        Spot spot = this.spotFixture.createSpot();

        Assignment assignment = this.assignmentFixture.createAssignment(
            user,
            spot,
            existingStart,
            existingEnd,
            "note"
        );

        String body = this.createBody(
            user.getId(),
            spot.getId(),
            newStart,
            newEnd,
            "Test: AssignmentFixture.createAssignment"
        );

        mockMvc.perform(post("/api/assignment")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )

            .andExpect(expectedStatus)
        ;
    }

    @Test
    void shouldReturnAssignmentById() throws Exception {
        Assignment assignment = this.assignmentFixture.createAssignment(
                "Zenobia",
                "Wyrywna",
                "Za rogiem",
                "2030-01-01",
                "2030-01-15",
                "Na wsze czasy"
        );

        mockMvc.perform(get("/api/assignment/{id}", assignment.getId())
                .with(user("admin").roles("ADMIN"))
            )

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.spotName").value("Za rogiem"))
            .andExpect(jsonPath("$.userName").value("Zenobia Wyrywna"))
            .andExpect(jsonPath("$.startDate").value("2030-01-01"))
            .andExpect(jsonPath("$.endDate").value("2030-01-15"))
            .andExpect(jsonPath("$.note").value("Na wsze czasy"))
        ;
    }

    @Test
    void shouldThrowNotFoundOnRandomUuidOnGetById() throws Exception {
        mockMvc.perform(get("/api/assignment/{id}", UUID.randomUUID())
                .with(user("admin").roles("ADMIN"))
            )
            .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldThrowForbiddenOnGetById() throws Exception {
        mockMvc.perform(get("/api/assignment/{id}", UUID.randomUUID())
                .with(user("user").roles("USER"))
            )
            .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldThrowUnauthorisedOnGetById() throws Exception {
        mockMvc.perform(get("/api/assignment/{id}", UUID.randomUUID())
            )
            .andExpect(status().isUnauthorized())
        ;
    }

    @Test
    void shouldUpdateAssignment() throws Exception {
        Assignment assignment = this.assignmentFixture.createAssignment(
                "Natalia",
                "Nieaktualna",
                "Na przystanku",
                "2030-01-01",
                "2030-01-15",
                "Note"
        );

        String body = this.createUpdateBody(
                "2030-01-01",
                "2030-01-10",
                "Bez 11-15"
        );

        mockMvc.perform(put("/api/assignment/{id}", assignment.getId())
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spotName").value("Na przystanku"))
                .andExpect(jsonPath("$.userName").value("Natalia Nieaktualna"))
                .andExpect(jsonPath("$.startDate").value("2030-01-01"))
                .andExpect(jsonPath("$.endDate").value("2030-01-10"))
                .andExpect(jsonPath("$.note").value("Bez 11-15"))
        ;
    }

    public static Stream<Arguments> overlapCasesForUpdate() {
        return Stream.of(
            Arguments.of(
                "2030-01-11",
                "2030-01-20",
                "2030-01-01",
                "2030-01-10",
                "2030-01-01",
                "2030-01-11",
                status().isConflict()
            ),
            Arguments.of(
                "2030-01-11",
                "2030-01-20",
                "2030-01-01",
                "2030-01-05",
                "2030-01-01",
                "2030-01-10",
                status().isOk()
            ),
            Arguments.of(
                "2030-01-11",
                "2030-01-20",
                "2030-01-21",
                "2030-01-30",
                "2030-01-20",
                "2030-01-30",
                status().isConflict()
            )
        );
    }

    @ParameterizedTest
    @MethodSource("overlapCasesForUpdate")
    void shouldDetectsOverlapOnUpdate(
            String existingStart,
            String existingEnd,
            String createdStart,
            String createdEnd,
            String updatedStart,
            String updatedEnd,
            ResultMatcher expectedStatus
    ) throws Exception {
        Assignment existingAssignment = this.assignmentFixture.createAssignment(
                "Natalia",
                "Nieaktualna",
                "Na przystanku",
                existingStart,
                existingEnd,
                "Existing Assignment"
        );

        User user = this.userFixture.createUser();

        Assignment createdAssignment = this.assignmentFixture.createAssignment(
                user,
                existingAssignment.getSpot(),
                createdStart,
                createdEnd,
                "Created Assignment"
        );

        String body = this.createUpdateBody(
                updatedStart,
                updatedEnd,
                "Updated Assignment"
        );

        mockMvc.perform(put("/api/assignment/{id}", createdAssignment.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(expectedStatus)
                ;
    }

    @Test
    void shouldThrowAssignmentNotFoundOnUpdateWithRandomUuid() throws Exception {

        String body = this.createUpdateBody(
                "2030-01-01",
                "2030-01-10",
                "Bez 11-15"
        );

        mockMvc.perform(put("/api/assignment/{id}", UUID.randomUUID())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldDeleteAssignment() throws Exception {
        Assignment assignment = this.assignmentFixture.createAssignment(
                "Elilia",
                "Znikla",
                "U stolu",
                "2030-01-01",
                "2030-01-15",
                "Note"
        );

        mockMvc.perform(delete("/api/assignment/{id}", assignment.getId())
                .with(user("admin").roles("ADMIN"))
            )
            .andExpect(status().isNoContent())
        ;
    }

    @Test
    void shouldThrowNotFoundOnRandomUuidWhenDeleteAssignment() throws Exception {

        mockMvc.perform(delete("/api/assignment/{id}", UUID.randomUUID())
                        .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    void shouldThrowForbiddenWithUserWhenDeleteAssignment() throws Exception {

        Assignment assignment = this.assignmentFixture.createAssignment(
                "Elilia",
                "Znikla",
                "U stolu",
                "2030-01-01",
                "2030-01-15",
                "Note"
        );

        mockMvc.perform(delete("/api/assignment/{id}", assignment.getId())
                        .with(user("user").roles("USER"))
                )
                .andExpect(status().isForbidden())
        ;
    }

    @Test
    void shouldThrowUnauthorisedWithoutUserWhenDeleteAssignment() throws Exception {

        Assignment assignment = this.assignmentFixture.createAssignment(
                "Elilia",
                "Znikla",
                "U stolu",
                "2030-01-01",
                "2030-01-15",
                "Note"
        );

        mockMvc.perform(delete("/api/assignment/{id}", assignment.getId())
                )
                .andExpect(status().isUnauthorized())
        ;
    }

    private String createBody(UUID userId, UUID spotId, String startDate, String endDate, String note) {
        String body = """
        {
          "userId": "%s",
          "spotId": "%s",
          "startDate": "%s"
        """;

        if (endDate != null) {
            body += ",\"endDate\": \"" + endDate + "\"";
        }

        if (note != null) {
            body += ",\"note\": \"" + note + "\"";
        }
        body += "}";

        return body.formatted(
                userId.toString(),
                spotId.toString(),
                startDate
        );
    }

    private String createUpdateBody(String startDate, String endDate, String note) {
        return """
        {
          "startDate": "%s",
          "endDate": "%s",
          "note": "%s"
        }
        """.formatted(
            startDate,
            endDate,
            note
        );
    }

}
