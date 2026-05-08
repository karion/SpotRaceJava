package pl.net.karion.SpotRacer.assignment.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import pl.net.karion.SpotRacer.assignment.model.AssignmentRepository;
import pl.net.karion.SpotRacer.spot.fixtures.SpotFixture;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.support.IntegrationTest;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.LocalDate;
import java.util.UUID;

public class AssignmentControllerTest  extends IntegrationTest {

//    private final AssignmentRepository assignmentRepository;
    @Autowired
    private UserFixture userFixture;

    @Autowired
    private SpotFixture spotFixture;

    @Autowired
    private MockMvc mockMvc;

//    public AssignmentControllerTest(
////            AssignmentRepository assignmentRepository,
//            UserFixture userFixture,
//            SpotFixture spotFixture
//    ) {
////        this.assignmentRepository = assignmentRepository;
//        this.userFixture = userFixture;
//        this.spotFixture = spotFixture;
//    }

    @Test
    void createAssignment() throws Exception {

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

    private String createBody(UUID userId, UUID spotId, String startDate, String endDate, String note) {
        return """
        {
          "userId": "%s",
          "spotId": "%s",
          "startDate": "%s",
          "endDate": "%s",
          "note": "%s"
        }
        """.formatted(
                userId.toString(),
                spotId.toString(),
                startDate,
                endDate,
                note
        );
    }

}
