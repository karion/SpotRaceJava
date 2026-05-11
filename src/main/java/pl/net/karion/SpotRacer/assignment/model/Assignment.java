package pl.net.karion.SpotRacer.assignment.model;

import jakarta.persistence.*;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.user.model.User;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "sp_assignment",
        indexes = {
                @Index(name = "idx_assignment_spot_start", columnList = "spot_id, start_date"),
                @Index(name = "idx_assignment_spot_end", columnList = "spot_id, end_date")
        }
)
public class Assignment {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private Spot spot;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = true)
    private LocalDate endDate;

    @Column(nullable = true)
    private String note;

    public Assignment() {
    }

    public Assignment(UUID id, User user, Spot spot, LocalDate startDate, LocalDate endDate, String note) {
        this.id = id;
        this.user = user;
        this.spot = spot;
        this.startDate = startDate;
        this.endDate = endDate;
        this.note = note;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
