package pl.net.karion.SpotRacer.company.api;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CompanyRequest(
    @NotNull
    String name,

    @NotNull
    boolean active
) {
}