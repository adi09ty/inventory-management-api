package com.adityakumar.inventory.service;

import com.adityakumar.inventory.domain.AppUser;
import com.adityakumar.inventory.domain.Role;
import com.adityakumar.inventory.dto.Dtos.AuthResponse;
import com.adityakumar.inventory.dto.Dtos.LoginRequest;
import com.adityakumar.inventory.dto.Dtos.RegisterRequest;
import com.adityakumar.inventory.exception.DuplicateResourceException;
import com.adityakumar.inventory.repository.UserRepository;
import com.adityakumar.inventory.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Self-registration always creates a USER; admins are seeded or promoted in the database. */
    public AuthResponse register(RegisterRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already taken: " + req.username());
        }
        AppUser user = users.save(new AppUser(req.username(), encoder.encode(req.password()), Role.USER));
        return tokenFor(user);
    }

    public AuthResponse login(LoginRequest req) {
        AppUser user = users.findByUsername(req.username())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        return tokenFor(user);
    }

    private AuthResponse tokenFor(AppUser user) {
        String token = jwt.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, "Bearer", user.getUsername(), user.getRole().name(), jwt.expiresInSeconds());
    }
}
