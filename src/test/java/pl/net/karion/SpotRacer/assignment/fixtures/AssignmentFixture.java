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
        Spot spot = this.spotFixture.createSpot("Spot create assignment");
        User user = this.userFixture.createUser(
                UserFixture.randomEmail(),
                "Hania",
                "Przypisówna"
        );

        Assignment assignment = new Assignment(
                UUID.randomUUID(),
                user,
                spot,
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 2, 1),
                "Test: AssignmentFixture.createAssignment"
        );

        return this.assignmentRepository.save(assignment);
    }

}
