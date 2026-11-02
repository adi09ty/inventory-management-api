package com.adityakumar.inventory.config;

import com.adityakumar.inventory.domain.AppUser;
import com.adityakumar.inventory.domain.Role;
import com.adityakumar.inventory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder,
                                @Value("${app.seed.admin-username}") String username,
                                @Value("${app.seed.admin-password}") String password) {
        return args -> {
            if (!users.existsByUsername(username)) {
                users.save(new AppUser(username, encoder.encode(password), Role.ADMIN));
            }
        };
    }
}
