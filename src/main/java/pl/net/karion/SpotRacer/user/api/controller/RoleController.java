package pl.net.karion.SpotRacer.user.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.user.service.UserService;

@Tag(name = "Users", description = "Operacje na użytkownikach")
@RestController
@RequestMapping("/api/user")
public class RoleController {

    private final UserService userService;

    public RoleController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{id}/role")
    public UserResponse addRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRolesRequest request
    ) {
        return userService.addRole(id, request.roles());
    }

    @DeleteMapping("/{id}/role")
    public UserResponse removeRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRolesRequest request
    ) {
        return userService.removeRole(id, request.roles());
    }
}
