package pl.net.karion.SpotRacer.spot.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity(name = "sp_spot")
public class Spot {


    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    private Location location;

    public Spot() {
    }

    public Spot(UUID id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
