package pl.net.karion.SpotRacer.reservation.service;

import pl.net.karion.SpotRacer.reservation.api.controller.ReservationResponse;
import pl.net.karion.SpotRacer.reservation.model.Reservation;

public class ReservationMapper {
    public static ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getUser().getId(),
            reservation.getUser().getFullname(),
            reservation.getSpot().getId(),
            reservation.getSpot().getName(),
            reservation.getDate()
        );
    }
}
