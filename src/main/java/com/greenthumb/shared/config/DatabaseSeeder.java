package com.greenthumb.shared.config;

import com.greenthumb.feature.care.CareSchedule;
import com.greenthumb.feature.care.CareScheduleRepository;
import com.greenthumb.feature.care.CareType;
import com.greenthumb.feature.plant.PlantStatus;
import com.greenthumb.feature.plant.UserPlant;
import com.greenthumb.feature.plant.UserPlantRepository;
import com.greenthumb.feature.species.PlantSpecies;
import com.greenthumb.feature.species.PlantSpeciesRepository;
import com.greenthumb.feature.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the database with initial data on first application startup.
 *
 * @author Hamza Ali
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PlantSpeciesRepository speciesRepository;
    private final UserPlantRepository plantRepository;
    private final CareScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Entry point called by Spring Boot after application context loads.
     * Skips seeding if data already exists.
     *
     * @param args command-line arguments (unused)
     */
    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding database...");
        seedUsers();
//        seedSpecies();
//        seedPlantsAndSchedules();
        log.info("Database seeding complete.");
    }

    /**
     * Creates 1 admin and 3 regular users with pre-verified accounts.
     */
    private void seedUsers() {
        // Admin account
        userRepository.save(User.builder()
                .name("GreenThumb Admin")
                .email("admin@greenthumb.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .bio("Platform administrator")
                .build());

        // Regular users
        userRepository.save(User.builder()
                .name("Hamza Ali")
                .email("hamza@greenthumb.com")
                .password(passwordEncoder.encode("User@1234"))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .bio("Plant enthusiast with a passion for tropical species")
                .location("Bahrain")
                .build());

        userRepository.save(User.builder()
                .name("Jasim Ahmed")
                .email("jasim@greenthumb.com")
                .password(passwordEncoder.encode("User@1234"))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .bio("Succulent collector")
                .location("Manama")
                .build());

        userRepository.save(User.builder()
                .name("Ali Hassan")
                .email("ali@greenthumb.com")
                .password(passwordEncoder.encode("User@1234"))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .bio("Beginner plant parent")
                .location("Riffa")
                .build());

        log.info("Users seeded: 1 admin + 3 users");
    }
}
