package pl.net.karion.SpotRacer.reservation.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pl.net.karion.SpotRacer.reservation.api.controller.ReservationRequest;
import pl.net.karion.SpotRacer.reservation.api.controller.ReservationResponse;
import pl.net.karion.SpotRacer.reservation.exception.ReservationAlreadyTakenException;
import pl.net.karion.SpotRacer.reservation.exception.ReservationNotFoundException;
import pl.net.karion.SpotRacer.reservation.exception.ReservationRequiresSelfOrAdminException;
import pl.net.karion.SpotRacer.reservation.model.Reservation;
import pl.net.karion.SpotRacer.reservation.model.ReservationRepository;
import pl.net.karion.SpotRacer.security.model.CurrentUser;
import pl.net.karion.SpotRacer.security.service.CurrentUserProvider;
import pl.net.karion.SpotRacer.spot.exception.SpotNotFoundException;
import pl.net.karion.SpotRacer.spot.model.Spot;
import pl.net.karion.SpotRacer.spot.model.SpotRepository;
import pl.net.karion.SpotRacer.user.exception.UserNotFoundException;
import pl.net.karion.SpotRacer.user.model.Role;
import pl.net.karion.SpotRacer.user.model.User;
import pl.net.karion.SpotRacer.user.model.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpotRepository spotRepository;
    private final CurrentUserProvider currentUserProvider;

    public ReservationService(
        ReservationRepository reservationRepository,
        UserRepository userRepository,
        SpotRepository spotRepository,
        CurrentUserProvider currentUserProvider
    ) {
        this.reservationRepository = reservationRepository;

        this.userRepository = userRepository;
        this.spotRepository = spotRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request) {

        User user = this.userRepository.findById(request.userId())
                .orElseThrow(UserNotFoundException::new);

        CurrentUser currentUser = this.currentUserProvider.currentUser();
        // test na uprawnienia
        if (!user.getId().equals(currentUser.id())) {
            if (currentUser.hasRole(Role.ADMIN)) {
                throw new ReservationRequiresSelfOrAdminException();
            }
        }

        Spot spot = this.spotRepository.findById(request.spotId())
                .orElseThrow(SpotNotFoundException::new);

        if (this.reservationRepository.existsBySpotIdAndDate(spot.getId(), request.date())) {
            throw new ReservationAlreadyTakenException();
        }

        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                user,
                spot,
                request.date()
        );

        try {
            Reservation saved = this.reservationRepository.save(reservation);
            return ReservationMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException cve) {
                if ("uk_reservation_spot_date".equals(cve.getConstraintName())) {
                    throw new ReservationAlreadyTakenException();
                }
            }
            throw ex;
        }
    }

    public void delete(UUID id) {
        Reservation reservation = this.reservationRepository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);

        CurrentUser currentUser = this.currentUserProvider.currentUser();
        // test na uprawnienia
        if (!reservation.getUser().getId().equals(currentUser.id())) {
            if (currentUser.hasRole(Role.ADMIN)) {
                throw new ReservationRequiresSelfOrAdminException();
            }
        }

        this.reservationRepository.delete(reservation);
    }
}
