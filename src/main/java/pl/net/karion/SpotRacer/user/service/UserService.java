package pl.net.karion.SpotRacer.user.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import pl.net.karion.SpotRacer.user.api.controller.CreateUserRequest;
import pl.net.karion.SpotRacer.user.api.controller.UpdateUserRequest;
import pl.net.karion.SpotRacer.user.api.controller.UserResponse;
import pl.net.karion.SpotRacer.user.model.Role;
import pl.net.karion.SpotRacer.user.model.User;
import pl.net.karion.SpotRacer.user.model.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User(
                UUID.randomUUID(),
                request.getEmail(),
                request.getFirstname(),
                request.getLastname()
        );

        user.addRole(Role.USER);

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    public UserResponse addRole(UUID id, List<Role> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        roles.forEach(user::addRole);

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    public UserResponse removeRole(UUID id, List<Role> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        roles.forEach(user::removeRole);

        if ((long) user.getRoles().size() < 1) {
            throw new IllegalArgumentException("User must have at least one role");
        }

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }
}