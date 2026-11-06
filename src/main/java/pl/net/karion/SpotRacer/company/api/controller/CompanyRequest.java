package pl.net.karion.SpotRacer.company.api.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompanyRequest(
    @NotBlank
    String name,

    @NotNull
    boolean active
) {
}