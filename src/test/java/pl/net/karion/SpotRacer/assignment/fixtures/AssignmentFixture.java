package pl.net.karion.SpotRacer.assignment.fixtures;

import org.springframework.stereotype.Component;
import pl.net.karion.SpotRacer.assignment.model.Assignment;
import pl.net.karion.SpotRacer.assignment.model.AssignmentRepository;
import pl.net.karion.SpotRacer.spot.fixtures.SpotFixture;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.user.fixture.UserFixture;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class AssignmentFixture {

    private final AssignmentRepository assignmentRepository;
    private final UserFixture userFixture;
    private final SpotFixture spotFixture;

    public AssignmentFixture(
            AssignmentRepository assignmentRepository,
            UserFixture userFixture,
            SpotFixture spotFixture
    ) {
        this.assignmentRepository = assignmentRepository;
        this.userFixture = userFixture;
        this.spotFixture = spotFixture;
    }

    public Assignment createAssignment() {
        return this.createAssignment(
                "Hania",
                "Przypisówna",
                "Spot create assignment",
                "2030-01-01",
                "2030-02-01",
                "Test: AssignmentFixture.createAssignment"
        );
    }

    public Assignment createAssignment(
            String userFirstname,
            String userLastname,
            String spotName
    ) {
        return this.createAssignment(
                userFirstname,
                userLastname,
                spotName,
                "2030-01-01",
                "2030-02-01",
                "Test: AssignmentFixture.createAssignment"
        );
    }

    public Assignment createAssignment(
            String userFirstname,
            String userLastname,
            String spotName,
            String startDate,
            String endDate,
            String note
    ) {
        Spot spot = this.spotFixture.createSpot(spotName);
        User user = this.userFixture.createUser(
                UserFixture.randomEmail(),
                userFirstname,
                userLastname
        );

        Assignment assignment = new Assignment(
                UUID.randomUUID(),
                user,
                spot,
                LocalDate.parse(startDate),
                endDate != null ? LocalDate.parse(endDate) : null,
                note
        );

        return this.assignmentRepository.save(assignment);
    }

    public Assignment createAssignment(
        User user,
        Spot spot,
        String startDate,
        String endDate,
        String note
    ) {
        Assignment assignment = new Assignment(
            UUID.randomUUID(),
            user,
            spot,
            LocalDate.parse(startDate),
            endDate != null ? LocalDate.parse(endDate): null,
            note
        );

        return this.assignmentRepository.save(assignment);
    }

}
