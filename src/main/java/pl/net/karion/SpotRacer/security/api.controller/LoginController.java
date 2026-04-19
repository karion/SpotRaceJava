package pl.net.karion.SpotRacer.security.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.net.karion.SpotRacer.security.model.UserDetails;
import pl.net.karion.SpotRacer.security.service.JwtService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Tag(name = "Auth", description = "Autoryzacja")
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginController(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.email(),
                        request.password()
                )
        );

        if (authentication.isAuthenticated()) {
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            if (principal == null) {
                throw new RuntimeException("Login failed");
            }
            String token = jwtService.generateToken(principal);

            return new TokenResponse(token, "Bearer", Instant.now().plus(1, ChronoUnit.HOURS).toString());
        } else {
            throw new RuntimeException("Login failed");
        }
    }
}
