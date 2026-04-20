package pl.net.karion.SpotRacer.spot.model;


import jakarta.persistence.*;

import java.util.UUID;

@Entity(name = "sp_location")
public class Location {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column()
    private String description;

    public Location() {
    }

    public Location(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
