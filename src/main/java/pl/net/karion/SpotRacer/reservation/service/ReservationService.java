package pl.net.karion.SpotRacer.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import pl.net.karion.SpotRacer.spot.service.LocationMapper;
import pl.net.karion.SpotRacer.user.exception.UserNotFoundException;
import pl.net.karion.SpotRacer.user.model.Role;
import pl.net.karion.SpotRacer.user.model.User;
import pl.net.karion.SpotRacer.user.model.UserRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpotRepository spotRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ReservationAvailabilityService reservationAvailabilityService;
    private final Clock clock;

    public ReservationService(
        ReservationRepository reservationRepository,
        UserRepository userRepository,
        SpotRepository spotRepository,
        CurrentUserProvider currentUserProvider,
        ReservationAvailabilityService reservationAvailabilityService,
        Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.spotRepository = spotRepository;
        this.currentUserProvider = currentUserProvider;
        this.reservationAvailabilityService = reservationAvailabilityService;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request) {

        User user = this.userRepository.findById(request.userId())
                .orElseThrow(UserNotFoundException::new);

        Spot spot = this.spotRepository.findById(request.spotId())
                .orElseThrow(SpotNotFoundException::new);


        this.reservationAvailabilityService.validateReservation(user, spot, request.date());

        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                user,
                spot,
                request.date()
        );

        try {
            Reservation saved = this.reservationRepository.saveAndFlush(reservation);
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
            if (!currentUser.hasRole(Role.ADMIN)) {
                throw new ReservationRequiresSelfOrAdminException();
            }
        }

        this.reservationRepository.delete(reservation);
    }

    public Page<ReservationResponse> getUserReservations(
        UUID userId,
        Pageable pageable
    ) {
        LocalDate today = LocalDate.now(this.clock);
        Specification<Reservation> spec = (root, query, cb) ->
            cb.and(
                cb.equal(root.get("user").get("id"), userId),
                cb.greaterThanOrEqualTo(root.get("date"), today)
            )
        ;
        Sort sort = Sort.by(
            Sort.Order.asc("date"),
            Sort.Order.asc("spot.location.name"),
            Sort.Order.asc("spot.name"),
            Sort.Order.asc("id")
        );

        Pageable sortedPageable = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sort
        );

        return this.reservationRepository
            .findAll(spec, sortedPageable)
            .map(ReservationMapper::toResponse);
    }
}
