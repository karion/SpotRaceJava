package pl.net.karion.SpotRacer.company.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Locale;
import java.util.UUID;

@Entity
@Table(
    name = "sp_company"
)
public class Company {
    @Id
    private UUID id;

    @Column(nullable = false, length = 250)
    private String name;

    @Column(nullable = false, length = 250)
    private String normalizedName;

    @Column
    private boolean active;

    public Company(String name, boolean active) {
        this.id = UUID.randomUUID();
        this.setName(name);
        this.active = active;
    }

    public Company() {
    }

    private String normalizeName(String name) {
        return name
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        String localName = name.replaceAll("\\s+", " ").trim();
        if (localName.length() > 250) {
            throw new IllegalArgumentException("name cannot be longer than 250 characters");
        }
        String normalizedName = normalizeName(name);

        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        this.name = localName;

        this.normalizedName = normalizedName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
