package pl.net.karion.SpotRacer.reservation.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.user.model.User;

@Entity
@Table(
        name = "sp_reservation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservation_spot_date",
                columnNames = {"spot_id", "date"}
        )
)
public class Reservation {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "spot_id")
    private Spot spot;

    @Column(nullable = false)
    private LocalDate date;

    public Reservation() {
    }

    public Reservation(UUID id, User user, Spot spot, LocalDate date) {
        this.id = id;
        this.user = user;
        this.spot = spot;
        this.date = date;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
