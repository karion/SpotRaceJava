package pl.net.karion.SpotRacer.company.api.controller;

import java.util.UUID;

public record CompanyResponse(
    UUID id,
    String name,
    String normalizedName,
    boolean active
) {
}