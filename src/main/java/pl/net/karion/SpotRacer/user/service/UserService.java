package pl.net.karion.SpotRacer.user.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pl.net.karion.SpotRacer.user.api.controller.CreateUserRequest;
import pl.net.karion.SpotRacer.user.api.controller.UpdateUserRequest;
import pl.net.karion.SpotRacer.user.api.controller.UserResponse;
import pl.net.karion.SpotRacer.user.exception.UserEmailTakenExceprion;
import pl.net.karion.SpotRacer.user.exception.UserMustHaveRoleException;
import pl.net.karion.SpotRacer.user.exception.UserNotFoundException;
import pl.net.karion.SpotRacer.user.model.Role;
import pl.net.karion.SpotRacer.user.model.User;
import pl.net.karion.SpotRacer.user.model.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserEmailTakenExceprion();
        }

        User user = new User(
                UUID.randomUUID(),
                request.getEmail(),
                request.getFirstname(),
                request.getLastname()
        );

        user.addRole(Role.USER);

        try {
            User savedUser = userRepository.save(user);

            return UserMapper.toResponse(savedUser);

        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException cve) {
                if ("uc_user_email".equals(cve.getConstraintName())) {
                    throw new UserEmailTakenExceprion();
                }
            }
            throw ex;
        }
    }

    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        return UserMapper.toResponse(user);
    }

    public Page<UserResponse> getUsers(String search, Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecifications.hasFirstname(search))
                .or(UserSpecifications.hasLastname(search))
                .or(UserSpecifications.hasEmail(search));

        return userRepository.findAll(spec, pageable)
                .map(UserMapper::toResponse);
    }

    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    public UserResponse addRole(UUID id, Set<Role> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        roles.forEach(user::addRole);

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    public UserResponse removeRole(UUID id, Set<Role> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        roles.forEach(user::removeRole);

        if ((long) user.getRoles().size() < 1) {
            throw new UserMustHaveRoleException();
        }

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }
}