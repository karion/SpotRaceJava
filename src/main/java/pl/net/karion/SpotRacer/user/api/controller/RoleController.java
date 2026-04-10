package pl.net.karion.SpotRacer.user.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import pl.net.karion.SpotRacer.user.model.Role;
import pl.net.karion.SpotRacer.user.service.UserService;

import java.util.List;
import java.util.UUID;

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
            @RequestBody List<Role> roles
    ) {
        return userService.addRole(id, roles);
    }

    @DeleteMapping("/{id}/role")
    public UserResponse removeRole(
            @PathVariable UUID id,
            @RequestBody List<Role> roles
    ) {
        return userService.removeRole(id, roles);
    }
}
